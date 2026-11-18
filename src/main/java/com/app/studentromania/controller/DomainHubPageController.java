package com.app.studentromania.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.app.studentromania.dao.FacultyDAO;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.model.FacultyProgram;
import com.app.studentromania.util.RequestUtils;

/**
 * Server-rendered "hub" pages at /facultati/{domainSlug} (e.g. /facultati/medicina):
 * every faculty in one study domain, grouped by city, each linking to its
 * /facultate/... profile. They target broad searches ("facultăți de medicină")
 * that no single faculty page can rank for, and give the faculty pages a
 * crawlable internal link with keyword anchor text.
 *
 * Faculties are matched the same way the search page's domain chips match them
 * (license OR master domain list contains the domain value), so the hub and
 * /search.html always show the same set. Romania (unistart.ro) only for now.
 */
@Controller
public class DomainHubPageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainHubPageController.class);

    private static final String HEAD_MARKER = "<!-- SSR_HEAD -->";
    private static final String CONTENT_MARKER = "<!-- SSR_HUB_CONTENT -->";

    /** Programs listed per faculty card before "și încă N". */
    private static final int PROGRAMS_PER_CARD = 5;
    private static final Locale RO = new Locale("ro");

    /** The intro mentions the review count only from this many reviews up. */
    private static final int MIN_REVIEWS_TO_MENTION = 10;
    /** Faculty list from Couchbase is re-read at most this often. */
    private static final long CACHE_MILLIS = 10 * 60 * 1000L;

    /** A published hub. {@code domainValue} must equal the stored domain string. */
    static final class Hub {
        final String slug;
        final String domainValue;
        /** Used in "Facultăți de {label}". */
        final String label;

        Hub(String slug, String domainValue, String label) {
            this.slug = slug;
            this.domainValue = domainValue;
            this.label = label;
        }
    }

    /**
     * Live hubs, in the order the /facultati index lists them. {@code domainValue}
     * is the stored domain string (compared after cedilla -> comma-below
     * normalisation, so either spelling works). Labels use the words people search
     * for ("drept", "litere"), not the official domain names. Each entry is also
     * listed in sitemap.xml and on the /facultati index.
     */
    static final Map<String, Hub> HUBS = new LinkedHashMap<>();
    static {
        add(new Hub("medicina", "Medicină", "Medicină"));
        add(new Hub("drept", "Științe Juridice", "Drept"));
        add(new Hub("economie", "Științe Economice", "Economie"));
        add(new Hub("inginerie", "Științe Inginerești", "Inginerie"));
        add(new Hub("matematica-informatica", "Științe Exacte", "Matematică, Informatică și Științe Exacte"));
        add(new Hub("psihologie", "Științe Sociale și Psihologie", "Psihologie și Științe Sociale"));
        add(new Hub("stiinte-politice-comunicare", "Științe Politice și Comunicare", "Științe Politice și Comunicare"));
        add(new Hub("litere", "Științe Umaniste", "Litere și Științe Umaniste"));
        add(new Hub("arte-arhitectura", "Arte Arhitectură și Urbanism", "Arte și Arhitectură"));
        add(new Hub("stiinte-naturii", "Științe ale Naturii", "Științe ale Naturii"));
        add(new Hub("agronomie", "Științe Agricole", "Agronomie și Științe Agricole"));
        add(new Hub("sport", "Ştiinţa Sportului şi Educaţiei Fizice", "Sport și Educație Fizică"));
        add(new Hub("teologie", "Teologie", "Teologie"));
        add(new Hub("militare", "Ştiinţe Militare Informaţii şi Ordine publică", "Științe Militare"));
    }

    private static void add(Hub hub) {
        HUBS.put(hub.slug, hub);
    }

    @Autowired
    private FacultyDAO facultyDAO;

    private String template;
    private String errorHtml;

    private volatile List<Faculty> cachedFaculties;
    private volatile long cachedAt;

    @PostConstruct
    public void init() throws IOException {
        template = load("templates/domain-hub.html");
        errorHtml = load("static/error.html");
    }

    @GetMapping({"/facultati/{domainSlug}", "/facultati/{domainSlug}/"})
    public void serveHub(@PathVariable String domainSlug, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        if (!"RO".equals(RequestUtils.resolveCountryCode(request))) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(errorHtml);
            return;
        }
        Hub hub = HUBS.get(domainSlug.toLowerCase(Locale.ROOT));
        if (hub == null) {
            // Not a domain: maybe a city hub (/facultati/cluj-napoca). Domain and city
            // slugs share the namespace; domain wins on a clash (none today).
            serveCity(domainSlug.toLowerCase(Locale.ROOT), request, response);
            return;
        }

        List<Faculty> faculties;
        try {
            faculties = facultiesInDomain(hub);
        } catch (RuntimeException e) {
            LOGGER.error("Hub /facultati/{} failed to load faculties", domainSlug, e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setHeader("Retry-After", "60");
            response.getWriter().write(errorHtml);
            return;
        }
        if (faculties.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(errorHtml);
            return;
        }

        String origin = RequestUtils.getOrigin(request);
        String canonicalUrl = origin + "/facultati/" + hub.slug;
        Map<String, List<Faculty>> byCity = groupByCity(faculties);

        String html = template
                .replace(HEAD_MARKER, buildHead(hub, faculties, byCity, origin, canonicalUrl))
                .replace(CONTENT_MARKER, buildContent(hub, faculties, byCity));
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(html);
    }

    // ---- city hubs ------------------------------------------------------

    /** A city gets its own hub from this many faculties up; below that the page is too thin. */
    static final int MIN_CITY_FACULTIES = 5;

    /** A city with enough faculties for a hub, keyed by ASCII slug ("targu-mures"). */
    static final class CityHub {
        final String slug;
        final String name;
        final List<Faculty> faculties;

        CityHub(String slug, String name, List<Faculty> faculties) {
            this.slug = slug;
            this.name = name;
            this.faculties = faculties;
        }
    }

    /**
     * City hubs, biggest first. Cities are grouped by slug, so "Piatra-Neamt" and
     * "Piatra Neamt" (or "Reșița"/"Resita") are one city; the display name is the
     * most common spelling, preferring one with diacritics.
     */
    Map<String, CityHub> cityHubs() {
        Map<String, List<Faculty>> bySlug = new LinkedHashMap<>();
        Map<String, Map<String, Integer>> spellings = new LinkedHashMap<>();
        for (Faculty f : publicRoFaculties()) {
            String name = cityName(f);
            if (StringUtils.isEmpty(name)) {
                continue;
            }
            String slug = slugify(name);
            bySlug.computeIfAbsent(slug, k -> new ArrayList<>()).add(f);
            spellings.computeIfAbsent(slug, k -> new LinkedHashMap<>()).merge(name, 1, Integer::sum);
        }
        List<CityHub> hubs = new ArrayList<>();
        for (Map.Entry<String, List<Faculty>> e : bySlug.entrySet()) {
            if (e.getValue().size() < MIN_CITY_FACULTIES || HUBS.containsKey(e.getKey())) {
                continue;
            }
            String best = null;
            int bestScore = -1;
            for (Map.Entry<String, Integer> s : spellings.get(e.getKey()).entrySet()) {
                boolean diacritics = !s.getKey().equals(Normalizer.normalize(s.getKey(), Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", ""));
                int score = (diacritics ? 100000 : 0) + s.getValue();
                if (score > bestScore) {
                    bestScore = score;
                    best = s.getKey();
                }
            }
            hubs.add(new CityHub(e.getKey(), best, e.getValue()));
        }
        hubs.sort((a, b) -> a.faculties.size() != b.faculties.size()
                ? b.faculties.size() - a.faculties.size()
                : a.name.compareToIgnoreCase(b.name));
        Map<String, CityHub> out = new LinkedHashMap<>();
        for (CityHub c : hubs) {
            out.put(c.slug, c);
        }
        return out;
    }

    /** "/facultati/cluj-napoca" if the faculty's city has a hub, else null. Used by faculty pages. */
    String cityHubPath(Faculty faculty) {
        String name = cityName(faculty);
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        try {
            CityHub hub = cityHubs().get(slugify(name));
            return hub != null ? "/facultati/" + hub.slug : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** City hub slugs, for the sitemap. */
    List<String> cityHubSlugs() {
        return new ArrayList<>(cityHubs().keySet());
    }

    private void serveCity(String slug, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, CityHub> cities;
        try {
            cities = cityHubs();
        } catch (RuntimeException e) {
            LOGGER.error("City hub /facultati/{} failed to load faculties", slug, e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setHeader("Retry-After", "60");
            response.getWriter().write(errorHtml);
            return;
        }
        CityHub city = cities.get(slug);
        if (city == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(errorHtml);
            return;
        }

        String origin = RequestUtils.getOrigin(request);
        String canonicalUrl = origin + "/facultati/" + city.slug;
        Map<String, List<Faculty>> byUni = groupByUniversity(city.faculties);
        Map<Hub, Integer> domains = domainCounts(city.faculties);
        List<String[]> faq = buildCityFaq(city, byUni, domains);

        String title = "Facultăți în " + city.name + ": universități, programe, păreri studenți | Unistart";
        String description = "Toate cele " + countPhrase(city.faculties.size(), "facultate", "facultăți") + " din "
                + city.name + ", la " + countPhrase(byUni.size(), "universitate", "universități") + ": "
                + topUniversities(byUni, 3) + ". Programe de licență și păreri de la studenți.";

        StringBuilder h = new StringBuilder();
        appendMeta(h, title, description, "Facultăți în " + city.name, origin, canonicalUrl);
        h.append("\t<script type=\"application/ld+json\">{\"@context\":\"https://schema.org\",\"@type\":\"BreadcrumbList\",\"itemListElement\":[")
                .append(crumb(1, "Acasă", origin + "/")).append(",")
                .append(crumb(2, "Facultăți", origin + "/facultati")).append(",")
                .append(crumb(3, city.name, canonicalUrl)).append("]}</script>\n");
        h.append("\t<script type=\"application/ld+json\">{\"@context\":\"https://schema.org\",\"@type\":\"ItemList\",\"name\":\"")
                .append(json("Facultăți în " + city.name)).append("\",\"numberOfItems\":").append(city.faculties.size())
                .append(",\"itemListElement\":[");
        int pos = 1;
        for (List<Faculty> list : byUni.values()) {
            for (Faculty f : list) {
                if (pos > 1) {
                    h.append(",");
                }
                h.append("{\"@type\":\"ListItem\",\"position\":").append(pos++)
                        .append(",\"url\":\"").append(json(origin + facultyPath(f))).append("\"")
                        .append(",\"name\":\"").append(json(fix(f.getFacultyName()))).append("\"}");
            }
        }
        h.append("]}</script>\n");
        appendFaqJsonLd(h, faq);

        StringBuilder c = new StringBuilder();
        c.append("<nav class=\"hub-breadcrumb\" aria-label=\"breadcrumb\"><a href=\"/\">Acasă</a> &rsaquo; ")
                .append("<a href=\"/facultati\">Facultăți</a> &rsaquo; <span>").append(esc(city.name)).append("</span></nav>\n");
        c.append("<h1>Facultăți în ").append(esc(city.name)).append("</h1>\n");
        c.append("<p class=\"hub-subtitle\">").append(countPhrase(city.faculties.size(), "facultate", "facultăți"))
                .append(" · ").append(countPhrase(byUni.size(), "universitate", "universități")).append("</p>\n");
        c.append("<p class=\"hub-intro\">Aici găsești toate facultățile din ").append(esc(city.name))
                .append(" listate pe Unistart, grupate pe universități. Cele mai multe sunt la ")
                .append(esc(topUniversities(byUni, 3))).append(". Deschide o facultate ca să vezi programele de licență și master,")
                .append(" locurile, taxele și părerile studenților.</p>\n");

        c.append("<ul class=\"hub-cities\" aria-label=\"Universități\">\n");
        for (Map.Entry<String, List<Faculty>> e : byUni.entrySet()) {
            String shortLabel = StringUtils.defaultIfBlank(FacultyProfilePageController.universityShortLabel(e.getKey()), e.getKey());
            c.append("  <li><a href=\"/facultati/").append(city.slug).append("#").append(uniAnchor(e.getValue().get(0)))
                    .append("\" title=\"").append(esc(e.getKey())).append("\">").append(esc(shortLabel))
                    .append(" (").append(e.getValue().size()).append(")</a></li>\n");
        }
        c.append("</ul>\n");

        for (Map.Entry<String, List<Faculty>> e : byUni.entrySet()) {
            c.append("<section class=\"hub-city\" id=\"").append(uniAnchor(e.getValue().get(0))).append("\">\n");
            c.append("  <h2>").append(esc(e.getKey())).append("</h2>\n");
            for (Faculty f : e.getValue()) {
                appendFacultyCard(c, f, false);
            }
            c.append("</section>\n");
        }

        if (!domains.isEmpty()) {
            // Domain hub links jump straight to this city's section of that hub.
            c.append("<div class=\"hub-faq\">\n  <h2>Pe domenii în ").append(esc(city.name)).append("</h2>\n  <ul class=\"hub-cities\">\n");
            for (Map.Entry<Hub, Integer> d : domains.entrySet()) {
                c.append("    <li><a href=\"/facultati/").append(d.getKey().slug).append("#").append(anchorId(city.name))
                        .append("\">").append(esc(d.getKey().label)).append(" (").append(d.getValue()).append(")</a></li>\n");
            }
            c.append("  </ul>\n</div>\n");
        }

        appendFaqHtml(c, faq);

        c.append("<div class=\"hub-faq\">\n  <h2>Alte orașe</h2>\n  <ul class=\"hub-cities\">\n");
        for (CityHub other : cities.values()) {
            if (!other.slug.equals(city.slug)) {
                c.append("    <li><a href=\"/facultati/").append(other.slug).append("\">").append(esc(other.name))
                        .append(" (").append(other.faculties.size()).append(")</a></li>\n");
            }
        }
        c.append("  </ul>\n</div>\n");
        appendCta(c, "Ai studiat sau studiezi în " + city.name + "? Spune-le viitorilor studenți cum e de fapt.");

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(template.replace(HEAD_MARKER, h.toString()).replace(CONTENT_MARKER, c.toString()));
    }

    /** University -> faculties, biggest university first; faculties by reviews, rating, name. */
    private static Map<String, List<Faculty>> groupByUniversity(List<Faculty> faculties) {
        Map<String, List<Faculty>> grouped = new LinkedHashMap<>();
        for (Faculty f : faculties) {
            String uni = fix(StringUtils.defaultIfBlank(f.getUniversityName(), "Alte facultăți")).trim();
            grouped.computeIfAbsent(uni, k -> new ArrayList<>()).add(f);
        }
        return sortGroups(grouped);
    }

    /** How many of these faculties fall in each live domain hub, most first. */
    private static Map<Hub, Integer> domainCounts(List<Faculty> faculties) {
        List<Map.Entry<Hub, Integer>> counts = new ArrayList<>();
        for (Hub hub : HUBS.values()) {
            String wanted = fix(hub.domainValue);
            int n = 0;
            for (Faculty f : faculties) {
                if (containsDomain(f.getFacultyDomainsLicense(), wanted) || containsDomain(f.getFacultyDomainsMaster(), wanted)) {
                    n++;
                }
            }
            if (n > 0) {
                counts.add(new java.util.AbstractMap.SimpleEntry<>(hub, n));
            }
        }
        counts.sort((a, b) -> b.getValue() - a.getValue());
        Map<Hub, Integer> out = new LinkedHashMap<>();
        for (Map.Entry<Hub, Integer> e : counts) {
            out.put(e.getKey(), e.getValue());
        }
        return out;
    }

    private List<String[]> buildCityFaq(CityHub city, Map<String, List<Faculty>> byUni, Map<Hub, Integer> domains) {
        List<String[]> faq = new ArrayList<>();
        faq.add(new String[] {
                "Câte facultăți sunt în " + city.name + "?",
                "Pe Unistart sunt listate " + countPhrase(city.faculties.size(), "facultate", "facultăți") + " în "
                        + city.name + ", la " + countPhrase(byUni.size(), "universitate", "universități") + "." });
        List<String> unis = new ArrayList<>();
        for (Map.Entry<String, List<Faculty>> e : byUni.entrySet()) {
            unis.add(e.getKey() + " (" + countPhrase(e.getValue().size(), "facultate", "facultăți") + ")");
        }
        faq.add(new String[] {
                "Ce universități sunt în " + city.name + "?",
                "Universitățile din " + city.name + " listate pe Unistart sunt: " + String.join(", ", unis) + "." });
        if (!domains.isEmpty()) {
            List<String> parts = new ArrayList<>();
            for (Map.Entry<Hub, Integer> d : domains.entrySet()) {
                parts.add(d.getKey().label.toLowerCase(RO) + " (" + d.getValue() + ")");
            }
            faq.add(new String[] {
                    "Ce se poate studia în " + city.name + "?",
                    "În " + city.name + " sunt facultăți din domeniile: " + String.join(", ", parts)
                            + ". Numărul arată câte facultăți au programe în fiecare domeniu; o facultate poate avea mai multe domenii." });
        }
        return faq;
    }

    private static String uniAnchor(Faculty f) {
        String base = StringUtils.isNotBlank(f.getUniversitySlug()) ? f.getUniversitySlug() : slugify(fix(f.getUniversityName()));
        return "univ-" + base;
    }

    /** /facultati - index of every domain hub with its faculty count. */
    @GetMapping({"/facultati", "/facultati/"})
    public void serveIndex(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        if (!"RO".equals(RequestUtils.resolveCountryCode(request))) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(errorHtml);
            return;
        }
        Map<Hub, List<Faculty>> byHub = new LinkedHashMap<>();
        try {
            for (Hub hub : HUBS.values()) {
                List<Faculty> list = facultiesInDomain(hub);
                if (!list.isEmpty()) {
                    byHub.put(hub, list);
                }
            }
        } catch (RuntimeException e) {
            LOGGER.error("Hub index /facultati failed to load faculties", e);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setHeader("Retry-After", "60");
            response.getWriter().write(errorHtml);
            return;
        }

        String origin = RequestUtils.getOrigin(request);
        String canonicalUrl = origin + "/facultati";
        Map<String, CityHub> cities = cityHubs();
        String title = "Facultăți din România pe domenii și orașe | Unistart";
        String description = "Toate facultățile din România, grupate pe " + countPhrase(byHub.size(), "domeniu", "domenii")
                + " (medicină, drept, economie, inginerie, informatică, psihologie...) și pe orașe (București, Cluj-Napoca, Iași,"
                + " Timișoara...). Programe de licență și păreri de la studenți.";

        StringBuilder h = new StringBuilder();
        appendMeta(h, title, description, "Facultăți din România, pe domenii și orașe", origin, canonicalUrl);
        h.append("\t<script type=\"application/ld+json\">{\"@context\":\"https://schema.org\",\"@type\":\"BreadcrumbList\",\"itemListElement\":[")
                .append(crumb(1, "Acasă", origin + "/")).append(",")
                .append(crumb(2, "Facultăți", canonicalUrl)).append("]}</script>\n");
        h.append("\t<script type=\"application/ld+json\">{\"@context\":\"https://schema.org\",\"@type\":\"ItemList\",\"itemListElement\":[");
        int pos = 1;
        for (Hub hub : byHub.keySet()) {
            if (pos > 1) {
                h.append(",");
            }
            h.append("{\"@type\":\"ListItem\",\"position\":").append(pos++)
                    .append(",\"url\":\"").append(json(origin + "/facultati/" + hub.slug)).append("\"")
                    .append(",\"name\":\"").append(json("Facultăți de " + hub.label)).append("\"}");
        }
        for (CityHub city : cities.values()) {
            h.append(",{\"@type\":\"ListItem\",\"position\":").append(pos++)
                    .append(",\"url\":\"").append(json(origin + "/facultati/" + city.slug)).append("\"")
                    .append(",\"name\":\"").append(json("Facultăți în " + city.name)).append("\"}");
        }
        h.append("]}</script>");

        StringBuilder c = new StringBuilder();
        c.append("<nav class=\"hub-breadcrumb\" aria-label=\"breadcrumb\"><a href=\"/\">Acasă</a> &rsaquo; <span>Facultăți</span></nav>\n");
        c.append("<h1>Facultăți din România, pe domenii și orașe</h1>\n");
        c.append("<p class=\"hub-intro\">Alege un domeniu sau un oraș ca să vezi toate facultățile lui,")
                .append(" cu programele de licență și părerile studenților. O facultate poate apărea în mai multe domenii.</p>\n");
        c.append("<ul class=\"hub-cities\" aria-label=\"Cuprins\"><li><a href=\"/facultati#pe-domenii\">Pe domenii</a></li>")
                .append("<li><a href=\"/facultati#pe-orase\">Pe orașe</a></li></ul>\n");
        c.append("<h2 id=\"pe-domenii\" class=\"hub-city\" style=\"font-size:21px;font-weight:600;\">Pe domenii</h2>\n");
        for (Map.Entry<Hub, List<Faculty>> e : byHub.entrySet()) {
            Hub hub = e.getKey();
            List<Faculty> list = e.getValue();
            c.append("<div class=\"hub-fac\">\n  <a class=\"hub-fac-name\" href=\"/facultati/").append(hub.slug).append("\">Facultăți de ")
                    .append(esc(hub.label)).append("</a>\n  <div class=\"hub-fac-meta\">")
                    .append(countPhrase(list.size(), "facultate", "facultăți")).append(" în ")
                    .append(countPhrase(groupByCity(list).size(), "oraș", "orașe")).append("</div>\n");
            List<String> programs = topPrograms(list, 5);
            if (!programs.isEmpty()) {
                c.append("  <div class=\"hub-fac-programs\">").append(esc(String.join(", ", programs))).append("</div>\n");
            }
            c.append("</div>\n");
        }
        if (!cities.isEmpty()) {
            c.append("<h2 id=\"pe-orase\" class=\"hub-city\" style=\"font-size:21px;font-weight:600;\">Pe orașe</h2>\n<ul class=\"hub-cities\">\n");
            for (CityHub city : cities.values()) {
                c.append("  <li><a href=\"/facultati/").append(city.slug).append("\">").append(esc(city.name))
                        .append(" (").append(city.faculties.size()).append(")</a></li>\n");
            }
            c.append("</ul>\n");
        }
        appendCta(c, "Ai studiat sau studiezi la o facultate din România? Spune-le viitorilor studenți cum e de fapt.");

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(template.replace(HEAD_MARKER, h.toString()).replace(CONTENT_MARKER, c.toString()));
    }

    // ---- data -----------------------------------------------------------

    private List<Faculty> facultiesInDomain(Hub hub) {
        String wanted = fix(hub.domainValue);
        List<Faculty> out = new ArrayList<>();
        for (Faculty f : publicRoFaculties()) {
            if (containsDomain(f.getFacultyDomainsLicense(), wanted) || containsDomain(f.getFacultyDomainsMaster(), wanted)) {
                out.add(f);
            }
        }
        return out;
    }

    /** Romanian faculties that have a public /facultate/... page. */
    private List<Faculty> publicRoFaculties() {
        List<Faculty> out = new ArrayList<>();
        for (Faculty f : allFaculties()) {
            String country = StringUtils.isNotEmpty(f.getCountryCode()) ? f.getCountryCode() : "RO";
            if ("RO".equalsIgnoreCase(country) && StringUtils.isNotEmpty(f.getUniversitySlug())
                    && StringUtils.isNotEmpty(f.getFacultySlug())) {
                out.add(f);
            }
        }
        return out;
    }

    private static boolean containsDomain(List<String> domains, String wanted) {
        if (domains == null) {
            return false;
        }
        for (String d : domains) {
            if (d != null && fix(d.trim()).equalsIgnoreCase(wanted)) {
                return true;
            }
        }
        return false;
    }

    private List<Faculty> allFaculties() {
        long now = System.currentTimeMillis();
        List<Faculty> cached = cachedFaculties;
        if (cached == null || now - cachedAt > CACHE_MILLIS) {
            cached = facultyDAO.getAllFaculties();
            cachedFaculties = cached;
            cachedAt = now;
        }
        return cached;
    }

    /** City -> faculties, cities by faculty count (desc) then name; faculties by reviews, rating, name. */
    private static Map<String, List<Faculty>> groupByCity(List<Faculty> faculties) {
        // Group by slug so "Piatra-Neamt"/"Piatra Neamt" merge; label with the best spelling.
        Map<String, List<Faculty>> bySlug = new LinkedHashMap<>();
        Map<String, String> label = new LinkedHashMap<>();
        for (Faculty f : faculties) {
            String city = cityName(f);
            if (StringUtils.isEmpty(city)) {
                city = "Alte orașe";
            }
            String slug = slugify(city);
            bySlug.computeIfAbsent(slug, k -> new ArrayList<>()).add(f);
            String prev = label.get(slug);
            if (prev == null || (!hasDiacritics(prev) && hasDiacritics(city))) {
                label.put(slug, city);
            }
        }
        Map<String, List<Faculty>> grouped = new LinkedHashMap<>();
        for (Map.Entry<String, List<Faculty>> e : bySlug.entrySet()) {
            grouped.put(label.get(e.getKey()), e.getValue());
        }
        return sortGroups(grouped);
    }

    /** Groups biggest first (then by name); inside a group: most reviews, best rating, name. */
    private static Map<String, List<Faculty>> sortGroups(Map<String, List<Faculty>> grouped) {
        List<Map.Entry<String, List<Faculty>>> entries = new ArrayList<>(grouped.entrySet());
        entries.sort((a, b) -> a.getValue().size() != b.getValue().size()
                ? b.getValue().size() - a.getValue().size()
                : a.getKey().compareToIgnoreCase(b.getKey()));
        Map<String, List<Faculty>> sorted = new LinkedHashMap<>();
        for (Map.Entry<String, List<Faculty>> e : entries) {
            List<Faculty> list = e.getValue();
            list.sort((a, b) -> {
                int ra = reviews(a);
                int rb = reviews(b);
                if (ra != rb) {
                    return rb - ra;
                }
                double aa = a.getAvgRating() != null ? a.getAvgRating() : 0;
                double ab = b.getAvgRating() != null ? b.getAvgRating() : 0;
                if (Double.compare(ab, aa) != 0) {
                    return Double.compare(ab, aa);
                }
                return fix(a.getFacultyName()).compareToIgnoreCase(fix(b.getFacultyName()));
            });
            sorted.put(e.getKey(), list);
        }
        return sorted;
    }

    // ---- <head> ---------------------------------------------------------

    private String buildHead(Hub hub, List<Faculty> faculties, Map<String, List<Faculty>> byCity, String origin,
            String canonicalUrl) {
        String title = "Facultăți de " + hub.label + ": listă pe orașe, păreri studenți | Unistart";
        String description = "Toate cele " + countPhrase(faculties.size(), "facultate", "facultăți") + " de "
                + hub.label.toLowerCase(new Locale("ro")) + " din România, pe orașe: " + topCities(byCity, 4)
                + ". Programe de licență și păreri de la studenți.";
        StringBuilder h = new StringBuilder();
        appendMeta(h, title, description, "Facultăți de " + hub.label + " din România", origin, canonicalUrl);

        // BreadcrumbList
        h.append("\t<script type=\"application/ld+json\">{\"@context\":\"https://schema.org\",\"@type\":\"BreadcrumbList\",\"itemListElement\":[")
                .append(crumb(1, "Acasă", origin + "/")).append(",")
                .append(crumb(2, "Facultăți", origin + "/facultati")).append(",")
                .append(crumb(3, "Facultăți de " + hub.label, canonicalUrl))
                .append("]}</script>\n");

        // ItemList of the faculty pages, in page order.
        h.append("\t<script type=\"application/ld+json\">{\"@context\":\"https://schema.org\",\"@type\":\"ItemList\",\"name\":\"")
                .append(json("Facultăți de " + hub.label + " din România")).append("\",\"numberOfItems\":")
                .append(faculties.size()).append(",\"itemListElement\":[");
        int pos = 1;
        for (List<Faculty> list : byCity.values()) {
            for (Faculty f : list) {
                if (pos > 1) {
                    h.append(",");
                }
                h.append("{\"@type\":\"ListItem\",\"position\":").append(pos++)
                        .append(",\"url\":\"").append(json(origin + facultyPath(f))).append("\"")
                        .append(",\"name\":\"").append(json(fix(f.getFacultyName()))).append("\"}");
            }
        }
        h.append("]}</script>\n");

        appendFaqJsonLd(h, buildFaq(hub, faculties, byCity));
        return h.toString();
    }

    /** title, description, canonical, Open Graph and Twitter tags. */
    private static void appendMeta(StringBuilder h, String title, String description, String socialTitle, String origin,
            String canonicalUrl) {
        String d = esc(description);
        String u = esc(canonicalUrl);
        String st = esc(socialTitle);
        String img = esc(origin + "/assets/img/alege-facultatea-potrivita.png");
        h.append("<title>").append(esc(title)).append("</title>\n");
        h.append("\t<meta name=\"description\" content=\"").append(d).append("\">\n");
        h.append("\t<link rel=\"canonical\" href=\"").append(u).append("\">\n");
        h.append("\t<meta property=\"og:type\" content=\"website\">\n");
        h.append("\t<meta property=\"og:site_name\" content=\"Unistart\">\n");
        h.append("\t<meta property=\"og:locale\" content=\"ro_RO\">\n");
        h.append("\t<meta property=\"og:title\" content=\"").append(st).append("\">\n");
        h.append("\t<meta property=\"og:description\" content=\"").append(d).append("\">\n");
        h.append("\t<meta property=\"og:url\" content=\"").append(u).append("\">\n");
        h.append("\t<meta property=\"og:image\" content=\"").append(img).append("\">\n");
        h.append("\t<meta property=\"og:image:width\" content=\"1940\">\n");
        h.append("\t<meta property=\"og:image:height\" content=\"824\">\n");
        h.append("\t<meta name=\"twitter:card\" content=\"summary_large_image\">\n");
        h.append("\t<meta name=\"twitter:title\" content=\"").append(st).append("\">\n");
        h.append("\t<meta name=\"twitter:description\" content=\"").append(d).append("\">\n");
        h.append("\t<meta name=\"twitter:image\" content=\"").append(img).append("\">\n");
    }

    private static String crumb(int position, String name, String url) {
        return "{\"@type\":\"ListItem\",\"position\":" + position + ",\"name\":\"" + json(name)
                + "\",\"item\":\"" + json(url) + "\"}";
    }

    // ---- body -----------------------------------------------------------

    private String buildContent(Hub hub, List<Faculty> faculties, Map<String, List<Faculty>> byCity) {
        int rated = 0;
        int reviewTotal = 0;
        Set<String> universities = new LinkedHashSet<>();
        for (Faculty f : faculties) {
            if (reviews(f) > 0) {
                rated++;
                reviewTotal += reviews(f);
            }
            universities.add(fix(StringUtils.defaultString(f.getUniversityName())));
        }

        StringBuilder html = new StringBuilder();
        html.append("<nav class=\"hub-breadcrumb\" aria-label=\"breadcrumb\"><a href=\"/\">Acasă</a> &rsaquo; ")
                .append("<a href=\"/facultati\">Facultăți</a> &rsaquo; <span>").append(esc(hub.label)).append("</span></nav>\n");
        html.append("<h1>Facultăți de ").append(esc(hub.label)).append(" din România</h1>\n");
        html.append("<p class=\"hub-subtitle\">").append(countPhrase(faculties.size(), "facultate", "facultăți"))
                .append(" · ").append(countPhrase(byCity.size(), "oraș", "orașe"))
                .append(" · ").append(countPhrase(universities.size(), "universitate", "universități")).append("</p>\n");

        html.append("<p class=\"hub-intro\">Aici găsești toate facultățile din domeniul ").append(esc(hub.label))
                .append(" listate pe Unistart");
        List<String> introPrograms = topPrograms(faculties, 5);
        if (!introPrograms.isEmpty()) {
            html.append(", cu programe precum ").append(esc(String.join(", ", introPrograms)));
        }
        html.append(". Cele mai multe sunt în ")
                .append(esc(topCities(byCity, 3))).append(". ");
        // Only once it's a number worth showing - "2 evaluări în total" reads as an empty site.
        if (reviewTotal >= MIN_REVIEWS_TO_MENTION) {
            html.append(countPhrase(rated, "facultate are", "facultăți au")).append(" deja evaluări de la studenți (")
                    .append(countPhrase(reviewTotal, "evaluare", "evaluări")).append(" în total). ");
        }
        html.append("Deschide o facultate ca să vezi programele de licență și master, locurile, taxele și părerile studenților.</p>\n");

        html.append("<ul class=\"hub-cities\" aria-label=\"Orașe\">\n");
        for (Map.Entry<String, List<Faculty>> e : byCity.entrySet()) {
            html.append("  <li><a href=\"").append(esc(currentPathAnchor(hub, e.getKey()))).append("\">")
                    .append(esc(e.getKey())).append(" (").append(e.getValue().size()).append(")</a></li>\n");
        }
        html.append("</ul>\n");

        Map<String, CityHub> cityHubsBySlug = cityHubs();
        for (Map.Entry<String, List<Faculty>> e : byCity.entrySet()) {
            html.append("<section class=\"hub-city\" id=\"").append(anchorId(e.getKey())).append("\">\n");
            html.append("  <h2>").append(esc(hub.label)).append(" în ").append(esc(e.getKey())).append("</h2>\n");
            for (Faculty f : e.getValue()) {
                appendFacultyCard(html, f, true);
            }
            CityHub cityHub = cityHubsBySlug.get(slugify(e.getKey()));
            if (cityHub != null) {
                html.append("  <p class=\"hub-city-all\"><a href=\"/facultati/").append(cityHub.slug)
                        .append("\">Toate facultățile din ").append(esc(cityHub.name)).append(" &rsaquo;</a></p>\n");
            }
            html.append("</section>\n");
        }

        appendFaqHtml(html, buildFaq(hub, faculties, byCity));

        html.append("<div class=\"hub-faq\">\n  <h2>Alte domenii</h2>\n  <ul class=\"hub-cities\">\n");
        for (Hub other : HUBS.values()) {
            if (other != hub) {
                html.append("    <li><a href=\"/facultati/").append(other.slug).append("\">").append(esc(other.label))
                        .append("</a></li>\n");
            }
        }
        html.append("  </ul>\n</div>\n");

        appendCta(html, "Ai studiat sau studiezi la una dintre aceste facultăți? Spune-le viitorilor studenți cum e de fapt.");
        return html.toString();
    }

    private void appendFacultyCard(StringBuilder html, Faculty f, boolean showUniversity) {
        html.append("  <div class=\"hub-fac\">\n");
        html.append("    <a class=\"hub-fac-name\" href=\"").append(esc(facultyPath(f))).append("\">")
                .append(esc(fix(f.getFacultyName())));
        if (StringUtils.isNotBlank(f.getFacultyShortname())) {
            html.append(" (").append(esc(fix(f.getFacultyShortname().trim()))).append(")");
        }
        html.append("</a>\n");
        if (showUniversity && StringUtils.isNotBlank(f.getUniversityName())) {
            html.append("    <div class=\"hub-fac-uni\">").append(esc(fix(f.getUniversityName()))).append("</div>\n");
        }

        html.append("    <div class=\"hub-fac-meta\">");
        int n = reviews(f);
        if (n > 0 && f.getAvgRating() != null) {
            html.append("<i class=\"fa fa-star\" aria-hidden=\"true\"></i> <strong>")
                    .append(String.format(Locale.ROOT, "%.1f", f.getAvgRating())).append("/5</strong> · ")
                    .append(countPhrase(n, "evaluare", "evaluări"));
            // A percentage from 1-2 reviews ("0% ar recomanda") says more about one person than the faculty.
            if (f.getPercentageWouldRecommend() != null && n >= 3) {
                html.append(" · ").append(f.getPercentageWouldRecommend()).append("% ar recomanda");
            }
        } else {
            html.append("Nicio evaluare încă · <a href=\"/review.html?faculty=").append(esc(f.getFacultyId()))
                    .append("\" rel=\"nofollow\">Scrie prima evaluare</a>");
        }
        html.append("</div>\n");

        List<String> programs = licenseProgramNames(f);
        if (!programs.isEmpty()) {
            html.append("    <div class=\"hub-fac-programs\">Licență: ");
            int shown = Math.min(PROGRAMS_PER_CARD, programs.size());
            html.append(esc(String.join(", ", programs.subList(0, shown))));
            if (programs.size() > shown) {
                html.append(" și încă ").append(programs.size() - shown);
            }
            html.append("</div>\n");
        }
        html.append("  </div>\n");
    }

    /**
     * FAQ answers are built from the same data the page lists, so they stay true
     * as faculties/reviews change. Rendered visibly and as FAQPage JSON-LD.
     */
    private List<String[]> buildFaq(Hub hub, List<Faculty> faculties, Map<String, List<Faculty>> byCity) {
        String label = hub.label.toLowerCase(new Locale("ro"));
        List<String[]> faq = new ArrayList<>();

        faq.add(new String[] {
                "Câte facultăți de " + label + " sunt în România?",
                "Pe Unistart sunt listate " + countPhrase(faculties.size(), "facultate", "facultăți") + " din domeniul "
                        + hub.label + ", în " + countPhrase(byCity.size(), "oraș", "orașe")
                        + ", la universități de stat și private." });

        List<String> cityParts = new ArrayList<>();
        for (Map.Entry<String, List<Faculty>> e : byCity.entrySet()) {
            cityParts.add(e.getKey() + " (" + e.getValue().size() + ")");
        }
        faq.add(new String[] {
                "În ce orașe se poate studia " + label + "?",
                "Facultăți din domeniul " + hub.label + " există în: " + String.join(", ", cityParts) + "." });

        List<String> names = topPrograms(faculties, 8);
        if (!names.isEmpty()) {
            faq.add(new String[] {
                    "Ce programe de licență se pot urma în domeniul " + hub.label + "?",
                    "Cele mai răspândite programe de licență la aceste facultăți sunt: " + String.join(", ", names)
                            + ". Lista completă a programelor, cu locurile și taxele, e pe pagina fiecărei facultăți." });
        }
        return faq;
    }

    // ---- helpers --------------------------------------------------------

    /**
     * The most common licence programs across these faculties, most frequent first,
     * case-insensitively merged; foreign-language duplicates ("Medicină (în limba
     * engleză)") are left out so the list reads as distinct subjects.
     */
    private static List<String> topPrograms(List<Faculty> faculties, int n) {
        Map<String, String> display = new LinkedHashMap<>();
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Faculty f : faculties) {
            for (String raw : licenseProgramNames(f)) {
                // "Drept (ID)", "Drept (IFR)", "Drept - Învățământ la distanță" -> "Drept"
                String name = STUDY_FORM_SUFFIX.matcher(raw).replaceAll("").trim();
                if (name.isEmpty()) {
                    continue;
                }
                String key = name.toLowerCase(Locale.ROOT);
                if (FOREIGN_LANGUAGE_PROGRAM.matcher(key).find()) {
                    continue;
                }
                display.putIfAbsent(key, name);
                counts.merge(key, 1, Integer::sum);
            }
        }
        List<Map.Entry<String, Integer>> top = new ArrayList<>(counts.entrySet());
        top.sort((x, y) -> y.getValue() - x.getValue());
        List<String> out = new ArrayList<>();
        for (int i = 0; i < Math.min(n, top.size()); i++) {
            out.add(display.get(top.get(i).getKey()));
        }
        return out;
    }

    private static final java.util.regex.Pattern STUDY_FORM_SUFFIX = java.util.regex.Pattern.compile(
            "(?i)\\s*\\((IF|ID|IFR|FR|zi)\\)|\\s*-\\s*(învățământ|invatamant)\\s+(la\\s+distanță|la\\s+distanta|cu\\s+frecvență\\s+redusă).*$");

    private static final java.util.regex.Pattern FOREIGN_LANGUAGE_PROGRAM = java.util.regex.Pattern.compile(
            "\\(.*(limba|engl|francez|german|maghiar).*\\)|în limba|in limba");

    /** Distinct licence program names, cleaned, in stored order. */
    private static List<String> licenseProgramNames(Faculty f) {
        Set<String> names = new LinkedHashSet<>();
        if (f.getLicensePrograms() != null) {
            for (FacultyProgram p : f.getLicensePrograms()) {
                String name = p != null ? StringUtils.normalizeSpace(fix(p.getProgramName())) : null;
                // Some rows are stored in capitals ("INFORMATICĂ").
                if (name != null && name.length() > 3 && name.equals(name.toUpperCase(RO))
                        && !name.equals(name.toLowerCase(RO))) {
                    String lower = name.toLowerCase(RO);
                    name = lower.substring(0, 1).toUpperCase(RO) + lower.substring(1);
                }
                if (StringUtils.isNotBlank(name)) {
                    names.add(name);
                }
            }
        }
        return new ArrayList<>(names);
    }

    private static String facultyPath(Faculty f) {
        return "/facultate/" + f.getUniversitySlug() + "/" + f.getFacultySlug();
    }

    private static int reviews(Faculty f) {
        return f.getCountRev() != null ? f.getCountRev() : 0;
    }

    /** "Bucureşti" / "Braşov, Braşov" -> "București" / "Brașov". */
    private static String cityName(Faculty f) {
        String city = StringUtils.defaultString(f.getFacultyCity());
        int comma = city.indexOf(',');
        return fix((comma > 0 ? city.substring(0, comma) : city).trim());
    }

    private static String topCities(Map<String, List<Faculty>> byCity, int n) {
        return topGroups(byCity, n);
    }

    /** "UBB (21), UTCN (9) și USAMV (5)" - like topGroups, with short university names. */
    private static String topUniversities(Map<String, List<Faculty>> byUni, int n) {
        Map<String, List<Faculty>> shortNamed = new LinkedHashMap<>();
        for (Map.Entry<String, List<Faculty>> e : byUni.entrySet()) {
            String label = FacultyProfilePageController.universityShortLabel(e.getKey());
            shortNamed.put(StringUtils.isNotBlank(label) && !shortNamed.containsKey(label) ? label : e.getKey(), e.getValue());
        }
        return topGroups(shortNamed, n);
    }

    /** "A (8), B (5) și C (4)" from the first n groups. */
    private static String topGroups(Map<String, List<Faculty>> groups, int n) {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, List<Faculty>> e : groups.entrySet()) {
            if (parts.size() == n) {
                break;
            }
            parts.add(e.getKey() + " (" + e.getValue().size() + ")");
        }
        if (parts.size() <= 1) {
            return String.join("", parts);
        }
        return String.join(", ", parts.subList(0, parts.size() - 1)) + " și " + parts.get(parts.size() - 1);
    }

    /** Romanian number agreement: 1 facultate, 5 facultăți, 20 de facultăți. */
    static String countPhrase(int n, String singular, String plural) {
        if (n == 1) {
            return "1 " + singular;
        }
        int lastTwo = n % 100;
        return n + (n >= 20 && (lastTwo == 0 || lastTwo >= 20) ? " de " : " ") + plural;
    }

    /** In-page anchor link; the hub path is repeated because the page has &lt;base href="/"&gt;. */
    private static String currentPathAnchor(Hub hub, String city) {
        return "/facultati/" + hub.slug + "#" + anchorId(city);
    }

    private static String anchorId(String city) {
        return "oras-" + slugify(city);
    }

    /** "Târgu Mureș" -> "targu-mures". */
    static String slugify(String s) {
        String ascii = Normalizer.normalize(StringUtils.defaultString(s), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return ascii.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private static boolean hasDiacritics(String s) {
        return !s.equals(Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", ""));
    }

    private static void appendFaqHtml(StringBuilder html, List<String[]> faq) {
        html.append("<div class=\"hub-faq\">\n  <h2>Întrebări frecvente</h2>\n");
        for (String[] qa : faq) {
            html.append("  <h3>").append(esc(qa[0])).append("</h3>\n  <p>").append(esc(qa[1])).append("</p>\n");
        }
        html.append("</div>\n");
    }

    /** FAQPage JSON-LD - must carry the same questions/answers as the visible FAQ block. */
    private static void appendFaqJsonLd(StringBuilder h, List<String[]> faq) {
        h.append("\t<script type=\"application/ld+json\">{\"@context\":\"https://schema.org\",\"@type\":\"FAQPage\",\"mainEntity\":[");
        for (int i = 0; i < faq.size(); i++) {
            if (i > 0) {
                h.append(",");
            }
            h.append("{\"@type\":\"Question\",\"name\":\"").append(json(faq.get(i)[0]))
                    .append("\",\"acceptedAnswer\":{\"@type\":\"Answer\",\"text\":\"").append(json(faq.get(i)[1])).append("\"}}");
        }
        h.append("]}</script>");
    }

    private static void appendCta(StringBuilder html, String text) {
        html.append("<div class=\"hub-cta\">\n  <p>").append(esc(text)).append("</p>\n")
                .append("  <a href=\"/review.html\" class=\"btn btn-warning btn-fill\">Lasă o evaluare</a>\n</div>\n");
    }

    private static String fix(String s) {
        return FacultyProfilePageController.fixRoDiacritics(StringUtils.defaultString(s));
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String json(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ")
                .replace("<", "\\u003C").replace(">", "\\u003E");
    }

    private static String load(String path) throws IOException {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        }
    }

    /** For the sitemap. */
    static List<String> hubSlugs() {
        return Collections.unmodifiableList(new ArrayList<>(HUBS.keySet()));
    }
}
