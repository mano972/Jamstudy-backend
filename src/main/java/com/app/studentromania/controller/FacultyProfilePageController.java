package com.app.studentromania.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.app.studentromania.dao.FacultyDAO;
import com.app.studentromania.dao.ReviewDAO;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.model.FacultyProgram;
import com.app.studentromania.model.Review;
import com.app.studentromania.util.FacultyFilter;
import com.app.studentromania.util.RequestUtils;
import com.app.studentromania.util.ReviewFilter;

/**
 * Serves the faculty profile page at the SEO-friendly path
 * /facultate/{universitySlug}/{facultySlug}, and 301-redirects the legacy
 * /profile.html?id=X links to it. No templating engine is used: the static
 * profile.html/error.html files are loaded once at startup and injected via
 * plain string replacement.
 */
@Controller
public class FacultyProfilePageController {

    private static final String TITLE_TAG = "<title>Unistart - Profil Facultate</title>";
    private static final String DESCRIPTION_TAG = "<meta name=\"description\" content=\"Unistart | Evaluari facultati. Alege facultatea potrivita pentru tine.\">";
    private static final String SOCIAL_META_START = "<!-- SOCIAL_META_START -->";
    private static final String SOCIAL_META_END = "<!-- SOCIAL_META_END -->";
    private static final String TOP_REVIEWS_MARKER = "<!-- SSR_TOP_REVIEWS -->";
    private static final String RELATED_MARKER = "<!-- SSR_RELATED_FACULTIES -->";
    private static final String BREADCRUMB_MARKER = "<!-- SSR_BREADCRUMB -->";
    private static final String OVERVIEW_MARKER = "<!-- SSR_FACULTY_OVERVIEW -->";

    /** Rows pulled from Couchbase; a few extra so we can skip text-less reviews. */
    private static final int TOP_REVIEWS_FETCH = 12;
    /** Review quotes rendered into the social-proof card and the JSON-LD. */
    private static final int TOP_REVIEWS_RENDER = 4;
    /**
     * Cap on a single quote's body. The visible card and the JSON-LD
     * {@code reviewBody} both go through {@link #reviewBody}, so they stay in
     * sync — Google requires the marked-up text to be the text shown on the page.
     */
    private static final int REVIEW_BODY_MAX_CHARS = 220;
    /** Sibling faculties linked in the "same university" group. */
    private static final int RELATED_SAME_UNIVERSITY = 8;
    /** Faculties linked in the "same domain" group (cross-university). */
    private static final int RELATED_SAME_DOMAIN = 6;
    /** How many same-domain rows to pull before filtering out self / already-listed. */
    private static final int DOMAIN_FETCH_LIMIT = 20;
    /** Faculties linked in the "same city" group (cross-university). */
    private static final int RELATED_SAME_CITY = 6;
    /** How many same-city rows to pull before filtering out self / already-listed. */
    private static final int CITY_FETCH_LIMIT = 20;

    @Autowired
    private FacultyDAO facultyDAO;

    @Autowired
    private ReviewDAO reviewDAO;

    private String profileHtmlTemplate;
    private String errorHtmlTemplate;

    @PostConstruct
    public void init() throws IOException {
        profileHtmlTemplate = loadClasspathResource("static/profile.html");
        errorHtmlTemplate = loadClasspathResource("static/error.html");
    }

    @GetMapping("/facultate/{universitySlug}/{facultySlug}")
    public void servePublicProfile(@PathVariable String universitySlug, @PathVariable String facultySlug,
            HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<Faculty> facultyOpt = facultyDAO.getByUniversitySlugAndFacultySlug(universitySlug, facultySlug);
        response.setContentType("text/html;charset=UTF-8");
        if (!facultyOpt.isPresent()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(errorHtmlTemplate);
            return;
        }
        String countryCode = RequestUtils.resolveCountryCode(request);
        Faculty faculty = facultyOpt.get();
        List<Review> topReviews = fetchTopReviews(faculty.getFacultyId(), countryCode);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(renderProfileHtml(faculty, universitySlug, facultySlug,
                RequestUtils.getOrigin(request), countryCode, topReviews));
    }

    /**
     * The most up-voted reviews for the faculty that actually carry review text,
     * capped at {@link #TOP_REVIEWS_RENDER}. Text-less reviews (rating only) are
     * skipped because they add nothing to either the rendered block or the
     * structured data. Country-scoped like every other public review listing.
     */
    private List<Review> fetchTopReviews(String facultyId, String countryCode) {
        ReviewFilter filter = new ReviewFilter(null, null, null, countryCode, null, "upvotes,desc",
                TOP_REVIEWS_FETCH, null, null);
        List<Review> withText = new ArrayList<>();
        try {
            for (Review review : reviewDAO.getFilteredReviewsByFacultyId(facultyId, filter)) {
                if (StringUtils.isNotBlank(review.getReviewText())) {
                    withText.add(review);
                }
                if (withText.size() == TOP_REVIEWS_RENDER) {
                    break;
                }
            }
        } catch (RuntimeException e) {
            // A profile page must still render if the review query fails.
            return new ArrayList<>();
        }
        return withText;
    }

    /**
     * Registering this mapping shadows Spring's default static-file serving of
     * /profile.html, so the no-id / not-found / pre-backfill cases must fall
     * through to the plain cached template to keep bare /profile.html working.
     */
    @GetMapping("/profile.html")
    public void serveLegacyProfile(@RequestParam(value = "id", required = false) String id,
            HttpServletResponse response) throws IOException {
        if (StringUtils.isNotEmpty(id)) {
            Optional<Faculty> facultyOpt = facultyDAO.getByFacultyId(id);
            if (facultyOpt.isPresent()) {
                Faculty faculty = facultyOpt.get();
                if (StringUtils.isNotEmpty(faculty.getUniversitySlug()) && StringUtils.isNotEmpty(faculty.getFacultySlug())) {
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    response.setHeader("Location", "/facultate/" + faculty.getUniversitySlug() + "/" + faculty.getFacultySlug());
                    return;
                }
            }
        }
        response.setContentType("text/html;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(profileHtmlTemplate);
    }

    private String renderProfileHtml(Faculty faculty, String universitySlug, String facultySlug, String origin,
            String countryCode, List<Review> topReviews) {
        String canonicalUrl = origin + "/facultate/" + universitySlug + "/" + facultySlug;
        String cityShort = cityShortName(faculty);
        String uniParen = StringUtils.isNotEmpty(faculty.getUniversityName())
                ? " (" + faculty.getUniversityName() + ")" : "";
        String shortnameParen = StringUtils.isNotEmpty(faculty.getFacultyShortname())
                ? " (" + faculty.getFacultyShortname() + ")" : "";
        String title = faculty.getFacultyName()
                + shortnameParen
                + (StringUtils.isNotEmpty(cityShort) ? ", " + cityShort : "")
                + uniParen
                + " — păreri și evaluări de la studenți | Unistart";
        String description = "Vezi evaluări, rating și detalii despre " + faculty.getFacultyName() + shortnameParen + uniParen
                + (StringUtils.isNotEmpty(cityShort) ? " din " + cityShort : "")
                + ". Alege facultatea potrivită pentru tine pe Unistart.";

        String html = profileHtmlTemplate.replace(TITLE_TAG,
                "<title>" + escapeHtml(title) + "</title>\n    <link rel=\"canonical\" href=\"" + canonicalUrl + "\">");
        html = html.replace(DESCRIPTION_TAG,
                "<meta name=\"description\" content=\"" + escapeHtml(description) + "\">");
        html = replaceSocialMeta(html, buildSocialMetaTags(faculty, description, canonicalUrl, origin, countryCode));
        html = replaceTopReviews(html, buildTopReviewsHtml(faculty, topReviews));
        html = replaceMarker(html, BREADCRUMB_MARKER, buildBreadcrumbHtml(faculty, origin));
        html = replaceMarker(html, OVERVIEW_MARKER, buildFacultyOverviewHtml(faculty));
        html = replaceMarker(html, RELATED_MARKER, buildRelatedFacultiesHtml(faculty, countryCode));
        html = html.replace("</head>",
                "<script>window.__FACULTY_ID__ = " + toJsStringLiteral(faculty.getFacultyId()) + ";</script>\n"
                + "<script type=\"application/ld+json\">" + buildJsonLd(faculty, canonicalUrl, origin, topReviews) + "</script>\n"
                + "<script type=\"application/ld+json\">" + buildBreadcrumbJsonLd(faculty, canonicalUrl, origin) + "</script>\n</head>");
        return html;
    }

    /**
     * A compact, server-rendered "what students say" card holding up to
     * {@link #TOP_REVIEWS_RENDER} short review quotes. The marker it replaces
     * lives inside #wrapper (just above the JS-built review summary), so:
     *   - crawlers get real review text straight from the HTML, and it backs the
     *     JSON-LD {@code review} array (schema.org requires the marked-up reviews
     *     to be visible on the page);
     *   - for humans it is hidden during load by {@code .wrapper.loading{opacity:0}}
     *     and shown, in place, once the page is ready — no separate JS teardown.
     * This replaces the old client-only "featured-review-quote".
     */
    private String buildTopReviewsHtml(Faculty faculty, List<Review> topReviews) {
        if (topReviews.isEmpty()) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        html.append("<div class=\"card\" id=\"ssr-top-reviews\">\n");
        html.append("  <div class=\"content\">\n");
        html.append("    <h2 style=\"font-size:18px;margin-top:0;\">")
                .append("<i class=\"fa fa-comments\" style=\"margin-right:10px;color:#898781;\"></i>")
                .append("<span data-i18n-key=\"featured-review-label\">Ce spun studenții</span></h2>\n");

        for (Review review : topReviews) {
            html.append("    <a href=\"#reviews\" onclick=\"goToReviewsTab(); return false;\"")
                    .append(" style=\"display:block;color:inherit;text-decoration:none;background-color:#f9f9f7;")
                    .append("border-radius:8px;padding:12px 16px;margin-bottom:10px;\">\n");
            html.append("      <i class=\"fa fa-quote-left\" style=\"color:#d9d6c9;margin-right:6px;\"></i>")
                    .append(escapeHtml(reviewBody(review))).append("\n");
            html.append("      <div style=\"margin-top:6px;color:#666;font-size:13px;\">");
            if (review.getGeneralRating() != null) {
                html.append("<i class=\"fa fa-star\" style=\"color:orange;\"></i> ")
                        .append("<strong style=\"color:orange;\">").append(review.getGeneralRating()).append("</strong>")
                        .append(" &middot; ");
            }
            html.append(escapeHtml(authorLabel(review)));
            html.append("</div>\n");
            html.append("    </a>\n");
        }

        html.append("    <div class=\"text-center\" style=\"margin-top:12px;\">\n");
        html.append("      <a href=\"#reviews\" class=\"btn btn-warning\" style=\"color:orange;\"")
                .append(" onclick=\"goToReviewsTab(); return false;\">")
                .append("<span data-i18n-key=\"see-all-reviews\">Vezi toate evaluările</span>");
        if (faculty.getCountRev() != null && faculty.getCountRev() > 0) {
            html.append(" (").append(faculty.getCountRev()).append(")");
        }
        html.append("</a>\n");
        html.append("    </div>\n");
        html.append("  </div>\n");
        html.append("</div>\n");
        return html.toString();
    }

    /**
     * Replaces the {@link #TOP_REVIEWS_MARKER} placeholder in profile.html with
     * the rendered card (or nothing, when the faculty has no text reviews). The
     * marker is expected to be present; if it was removed from the template the
     * html is returned unchanged rather than appended elsewhere, since the card
     * must sit inside #wrapper.
     */
    private String replaceTopReviews(String html, String topReviewsHtml) {
        if (html.contains(TOP_REVIEWS_MARKER)) {
            return html.replace(TOP_REVIEWS_MARKER, topReviewsHtml);
        }
        return html;
    }

    private String authorLabel(Review review) {
        String status = review.getUserStatus();
        if (status == null) {
            return "Student";
        }
        switch (status.trim().toUpperCase()) {
            case "MASTERAND":
                return "Masterand";
            case "ABSOLVENT":
            case "GRADUATE":
                return "Absolvent";
            case "ELEV":
                return "Elev";
            case "STUDENT":
            default:
                return "Student";
        }
    }

    private String reviewBody(Review review) {
        String text = review.getReviewText();
        if (text == null) {
            return "";
        }
        text = text.trim();
        if (text.length() > REVIEW_BODY_MAX_CHARS) {
            return text.substring(0, REVIEW_BODY_MAX_CHARS).trim() + "…";
        }
        return text;
    }

    private static String isoDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    // ---- faculty overview (main unique SSR content) --------------------

    /**
     * Server-rendered faculty overview: an {@code <h1>}, the faculty description,
     * a key-facts list and the licence / master programs tables. This is the
     * page's main unique content — without it the pre-JS HTML is near-identical
     * across every faculty (shared template + client-only data load), which is
     * what makes Google report "Duplicate, Google chose different canonical than
     * user". Sits inside #wrapper (so {@code .wrapper.loading{opacity:0}} hides it
     * while the page loads) and is removed by {@code getFaculty()} in profile.html
     * once the interactive render is ready, so users only ever see the JS version.
     * Rows with no value are dropped, so thin faculties don't all render the same
     * "N/A" boilerplate.
     */
    private String buildFacultyOverviewHtml(Faculty faculty) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"section section-white\" id=\"ssr-faculty-overview\">\n");
        html.append("  <div class=\"container-fluid\">\n    <div class=\"row\">\n");
        html.append("      <div class=\"col-lg-8 col-lg-offset-2 col-md-8 col-md-offset-2 col-sm-12 col-xs-12\">\n");
        html.append("        <div class=\"card\">\n          <div class=\"content\">\n");

        String cityShort = cityShortName(faculty);
        StringBuilder h1 = new StringBuilder(nvl(faculty.getFacultyName(), ""));
        if (StringUtils.isNotEmpty(faculty.getFacultyShortname())) {
            h1.append(" (").append(faculty.getFacultyShortname()).append(")");
        }
        if (StringUtils.isNotEmpty(faculty.getUniversityName())) {
            h1.append(" — ").append(faculty.getUniversityName());
        }
        if (StringUtils.isNotEmpty(cityShort)) {
            h1.append(", ").append(cityShort);
        }
        html.append("            <h1 style=\"font-size:24px;margin-top:0;\">").append(escapeHtml(h1.toString())).append("</h1>\n");

        if (StringUtils.isNotBlank(faculty.getFacultyDescription())) {
            html.append("            <p style=\"white-space:pre-line;\">")
                    .append(escapeHtml(faculty.getFacultyDescription().trim())).append("</p>\n");
        }

        List<String[]> facts = new ArrayList<>();
        addFact(facts, "Oraș", cityShort);
        addFact(facts, "Adresă", faculty.getFacultyAddress());
        addFact(facts, "Website", faculty.getFacultyWebsite());
        addFact(facts, "Tip", faculty.getFacultyType());
        addFact(facts, "Durata studiilor (ani)", faculty.getNoOfYears());
        addFact(facts, "Locuri licență", faculty.getAvailablePlacesLicense());
        addFact(facts, "Locuri buget licență", faculty.getBudgetPlacesLicense());
        addFact(facts, "Locuri taxă licență", faculty.getTaxPlacesLicense());
        addFact(facts, "Locuri masterat", faculty.getAvailablePlacesMaster());
        addFact(facts, "Studenți înscriși (licență)", faculty.getEnrolledStudentsLicence());
        addFact(facts, "Număr profesori", faculty.getNoOfProfessors());
        if (!facts.isEmpty()) {
            html.append("            <ul style=\"list-style:none;padding:0;margin:12px 0;\">\n");
            for (String[] fact : facts) {
                html.append("              <li style=\"padding:5px 0;border-top:1px solid #efefef;\"><strong>")
                        .append(escapeHtml(fact[0])).append(":</strong> ").append(escapeHtml(fact[1])).append("</li>\n");
            }
            html.append("            </ul>\n");
        }

        appendProgramsTable(html, "Programe de licență", faculty.getLicensePrograms());
        appendProgramsTable(html, "Programe de masterat", faculty.getMasterPrograms());

        // Raw-HTML internal links to the tool pages. The site nav/footer is built by
        // unistart.js, so without this the calculator / comparison pages have no
        // crawlable inbound link and Google is slow to index them. Faculty pages are
        // the most-crawled URLs, so this is where the link does the most good.
        html.append("            <p style=\"margin-top:14px;font-size:13px;color:#66615b;\">Resurse: ")
                .append("<a href=\"/calculator-medie-facultate.html\">Calculator medie facultate (ECTS)</a> &middot; ")
                .append("<a href=\"/comparison.html\">Compară facultăți</a></p>\n");

        html.append("          </div>\n        </div>\n      </div>\n    </div>\n  </div>\n</div>\n");
        return html.toString();
    }

    private static void addFact(List<String[]> facts, String label, Object value) {
        if (value == null) {
            return;
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty() || "0".equals(s)) {
            return;
        }
        facts.add(new String[] { label, s });
    }

    private void appendProgramsTable(StringBuilder html, String heading, List<FacultyProgram> programs) {
        if (programs == null || programs.isEmpty()) {
            return;
        }
        html.append("            <h2 style=\"font-size:18px;margin-top:22px;\">").append(escapeHtml(heading)).append("</h2>\n");
        html.append("            <div style=\"overflow-x:auto;\">\n");
        html.append("            <table style=\"width:100%;border-collapse:collapse;font-size:14px;\">\n");
        html.append("              <thead><tr>");
        for (String col : new String[] { "Program", "Domeniu", "Locuri", "Taxă anuală", "Candidați/loc", "Admitere", "Acreditare" }) {
            html.append("<th style=\"text-align:left;padding:6px;border-bottom:1px solid #ddd;\">").append(escapeHtml(col)).append("</th>");
        }
        html.append("</tr></thead>\n            <tbody>\n");
        for (FacultyProgram program : programs) {
            if (program == null) {
                continue;
            }
            html.append("              <tr>")
                    .append(td(program.getProgramName()))
                    .append(td(firstNonBlank(program.getProgramDomain(), program.getDomainOfLicenseOrMaster())))
                    .append(td(program.getProgramAvailablePlaces() != null ? String.valueOf(program.getProgramAvailablePlaces()) : null))
                    .append(td(program.getAnnualTax() != null ? program.getAnnualTax() + " lei/an" : null))
                    .append(td(program.getCandidatesPerPlace() != null ? String.valueOf(program.getCandidatesPerPlace()) : null))
                    .append(td(program.getAdmissionType()))
                    .append(td(program.getProgramAccreditation()))
                    .append("</tr>\n");
        }
        html.append("            </tbody>\n            </table>\n            </div>\n");
    }

    private static String td(String value) {
        return "<td style=\"padding:6px;border-bottom:1px solid #f0f0f0;\">"
                + escapeHtml(StringUtils.isNotBlank(value) ? value.trim() : "—") + "</td>";
    }

    private static String firstNonBlank(String a, String b) {
        if (StringUtils.isNotBlank(a)) {
            return a;
        }
        return StringUtils.isNotBlank(b) ? b : "";
    }

    // ---- related faculties (internal linking) --------------------------

    /**
     * A server-rendered block of {@code <a>} links to other faculties — same
     * university, and same first licence domain across universities — so a crawler
     * fetching /facultate/{u}/{f} gets real internal links with canonical slug
     * URLs and faculty-name anchor text. The page's JS {@code #other-faculties}
     * carousel is client-only and links via redirecting {@code ?id=} URLs; this is
     * the crawlable layer. The marker sits inside #wrapper (just after the JS
     * #other-faculties carousel), so it's hidden during load by
     * {@code .wrapper.loading{opacity:0}} and appears in place with the page.
     */
    private String buildRelatedFacultiesHtml(Faculty faculty, String countryCode) {
        List<Faculty> sameUni = sameUniversityFaculties(faculty);

        Set<String> shown = new LinkedHashSet<>();
        shown.add(faculty.getFacultyId());
        for (Faculty f : sameUni) {
            shown.add(f.getFacultyId());
        }

        List<Faculty> sameCity = sameCityFaculties(faculty, countryCode, shown);
        for (Faculty f : sameCity) {
            shown.add(f.getFacultyId());
        }

        List<Faculty> sameDomain = sameDomainFaculties(faculty, countryCode, shown);

        if (sameUni.isEmpty() && sameCity.isEmpty() && sameDomain.isEmpty()) {
            return "";
        }

        StringBuilder html = new StringBuilder();
        html.append("<div class=\"section section-white\">\n");
        html.append("  <div class=\"container-fluid\">\n");
        html.append("    <div class=\"row\">\n");
        html.append("      <div class=\"col-lg-8 col-lg-offset-2 col-md-8 col-md-offset-2 col-sm-12 col-xs-12\">\n");
        html.append("        <nav id=\"ssr-related-faculties\" class=\"card\" aria-label=\"Facultăți similare\">\n");
        html.append("          <div class=\"content\">\n");

        if (!sameUni.isEmpty()) {
            html.append("            <h2 style=\"font-size:19px;\">Alte facultăți la ")
                    .append(escapeHtml(nvl(faculty.getUniversityName(), "aceeași universitate")))
                    .append("</h2>\n");
            appendFacultyList(html, sameUni, false);
        }

        if (!sameCity.isEmpty()) {
            html.append("            <h2 style=\"font-size:19px;margin-top:22px;\">Facultăți în ")
                    .append(escapeHtml(cityShortName(faculty)))
                    .append("</h2>\n");
            appendFacultyList(html, sameCity, true);
        }

        if (!sameDomain.isEmpty()) {
            String domain = primaryDomain(faculty);
            html.append("            <h2 style=\"font-size:19px;margin-top:22px;\">")
                    .append(domain != null ? "Facultăți de " + escapeHtml(domain) : "Facultăți din același domeniu")
                    .append("</h2>\n");
            appendFacultyList(html, sameDomain, true);
        }

        html.append("          </div>\n        </nav>\n      </div>\n    </div>\n  </div>\n</div>\n");
        return html.toString();
    }

    private void appendFacultyList(StringBuilder html, List<Faculty> faculties, boolean showUniversity) {
        html.append("            <ul style=\"list-style:none;padding:0;margin:0;\">\n");
        for (Faculty f : faculties) {
            html.append("              <li style=\"padding:7px 0;border-top:1px solid #efefef;\">")
                    .append("<a href=\"/facultate/").append(f.getUniversitySlug()).append("/").append(f.getFacultySlug())
                    .append("\">").append(escapeHtml(f.getFacultyName()));
            if (StringUtils.isNotEmpty(f.getFacultyShortname())) {
                html.append(" (").append(escapeHtml(f.getFacultyShortname())).append(")");
            }
            html.append("</a>");
            if (showUniversity && StringUtils.isNotEmpty(f.getUniversityName())) {
                html.append(" <span style=\"color:#999;font-size:13px;\">— ")
                        .append(escapeHtml(f.getUniversityName())).append("</span>");
            }
            html.append("</li>\n");
        }
        html.append("            </ul>\n");
    }

    private List<Faculty> sameUniversityFaculties(Faculty faculty) {
        try {
            List<Faculty> out = new ArrayList<>();
            for (Faculty f : facultyDAO.getByUniversityId(faculty.getUniversityId())) {
                if (!f.getFacultyId().equals(faculty.getFacultyId()) && hasSlug(f)) {
                    out.add(f);
                }
            }
            out.sort((a, b) -> {
                int ra = a.getCountRev() != null ? a.getCountRev() : 0;
                int rb = b.getCountRev() != null ? b.getCountRev() : 0;
                if (ra != rb) {
                    return rb - ra;
                }
                double aa = a.getAvgRating() != null ? a.getAvgRating() : 0;
                double ab = b.getAvgRating() != null ? b.getAvgRating() : 0;
                if (Double.compare(ab, aa) != 0) {
                    return Double.compare(ab, aa);
                }
                return nvl(a.getFacultyName(), "").compareToIgnoreCase(nvl(b.getFacultyName(), ""));
            });
            return out.size() > RELATED_SAME_UNIVERSITY ? out.subList(0, RELATED_SAME_UNIVERSITY) : out;
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
    }

    private List<Faculty> sameDomainFaculties(Faculty faculty, String countryCode, Set<String> excludeIds) {
        String domain = primaryDomain(faculty);
        if (domain == null) {
            return new ArrayList<>();
        }
        try {
            FacultyFilter filter = new FacultyFilter(null, null, null, null,
                    Collections.singletonList(domain), null, null, null, countryCode, null,
                    DOMAIN_FETCH_LIMIT, null, null);
            List<Faculty> out = new ArrayList<>();
            for (Faculty f : facultyDAO.getFilteredFaculties(filter)) {
                if (excludeIds.contains(f.getFacultyId()) || !hasSlug(f)) {
                    continue;
                }
                out.add(f);
                if (out.size() == RELATED_SAME_DOMAIN) {
                    break;
                }
            }
            return out;
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Other faculties in the same city (any university), so "facultate {domeniu}
     * {oraș}" style queries have a crawlable internal cluster. Filters on the
     * stored {@code facultyCity} value verbatim (e.g. "Braşov, Braşov").
     */
    private List<Faculty> sameCityFaculties(Faculty faculty, String countryCode, Set<String> excludeIds) {
        if (StringUtils.isBlank(faculty.getFacultyCity())) {
            return new ArrayList<>();
        }
        try {
            FacultyFilter filter = new FacultyFilter(null, null, null,
                    Collections.singletonList(faculty.getFacultyCity()), null, null, null, null, countryCode, null,
                    CITY_FETCH_LIMIT, null, null);
            List<Faculty> out = new ArrayList<>();
            for (Faculty f : facultyDAO.getFilteredFaculties(filter)) {
                if (excludeIds.contains(f.getFacultyId()) || !hasSlug(f)) {
                    continue;
                }
                out.add(f);
                if (out.size() == RELATED_SAME_CITY) {
                    break;
                }
            }
            return out;
        } catch (RuntimeException e) {
            return new ArrayList<>();
        }
    }

    private String primaryDomain(Faculty faculty) {
        List<String> domains = faculty.getFacultyDomainsLicense();
        if (domains == null || domains.isEmpty()) {
            return null;
        }
        return StringUtils.isNotBlank(domains.get(0)) ? domains.get(0).trim() : null;
    }

    /** The city label without the trailing county, e.g. "Braşov, Braşov" -> "Braşov". */
    private static String cityShortName(Faculty faculty) {
        String city = faculty.getFacultyCity();
        if (StringUtils.isBlank(city)) {
            return "";
        }
        int comma = city.indexOf(',');
        return (comma > 0 ? city.substring(0, comma) : city).trim();
    }

    private static boolean hasSlug(Faculty f) {
        return StringUtils.isNotEmpty(f.getUniversitySlug()) && StringUtils.isNotEmpty(f.getFacultySlug());
    }

    private static String nvl(String value, String fallback) {
        return StringUtils.isNotBlank(value) ? value : fallback;
    }

    // ---- breadcrumb -----------------------------------------------------

    /**
     * Server-rendered breadcrumb (Acasă › oraș › universitate › facultate) placed
     * inside #wrapper, so a crawler gets the city / university anchor text and the
     * trail paints with the page. There are no city / university hub pages, so
     * those crumbs point at the search page pre-filled with the term — the same
     * thing the page's own searchByUniversityName() does. The last crumb is not a
     * link. Paired with {@link #buildBreadcrumbJsonLd}.
     */
    private String buildBreadcrumbHtml(Faculty faculty, String origin) {
        String city = cityShortName(faculty);
        String uni = nvl(faculty.getUniversityName(), "");

        StringBuilder html = new StringBuilder();
        html.append("<nav aria-label=\"breadcrumb\" class=\"ssr-breadcrumb\" ")
                .append("style=\"max-width:960px;margin:0 auto;padding:10px 15px;font-size:13px;color:#999;\">\n");
        html.append("  <a href=\"").append(escapeHtml(origin)).append("/\" style=\"color:#999;\">Acasă</a>\n");
        if (StringUtils.isNotEmpty(city)) {
            html.append("  <span> &rsaquo; </span>\n  <a href=\"").append(escapeHtml(origin))
                    .append("/search.html?q=").append(urlEncode(city)).append("\" style=\"color:#999;\">")
                    .append(escapeHtml(city)).append("</a>\n");
        }
        if (StringUtils.isNotEmpty(uni)) {
            html.append("  <span> &rsaquo; </span>\n  <a href=\"").append(escapeHtml(origin))
                    .append("/search.html?q=").append(urlEncode(uni)).append("\" style=\"color:#999;\">")
                    .append(escapeHtml(uni)).append("</a>\n");
        }
        html.append("  <span> &rsaquo; </span>\n  <span style=\"color:#666;\">")
                .append(escapeHtml(faculty.getFacultyName())).append("</span>\n");
        html.append("</nav>\n");
        return html.toString();
    }

    private String buildBreadcrumbJsonLd(Faculty faculty, String canonicalUrl, String origin) {
        StringBuilder json = new StringBuilder();
        json.append("{\"@context\":\"https://schema.org\",\"@type\":\"BreadcrumbList\",\"itemListElement\":[");
        int pos = 1;
        json.append(breadcrumbItem(pos++, "Acasă", origin + "/"));
        String city = cityShortName(faculty);
        if (StringUtils.isNotEmpty(city)) {
            json.append(",").append(breadcrumbItem(pos++, city, origin + "/search.html?q=" + urlEncode(city)));
        }
        if (StringUtils.isNotEmpty(faculty.getUniversityName())) {
            json.append(",").append(breadcrumbItem(pos++, faculty.getUniversityName(),
                    origin + "/search.html?q=" + urlEncode(faculty.getUniversityName())));
        }
        json.append(",").append(breadcrumbItem(pos, faculty.getFacultyName(), canonicalUrl));
        json.append("]}");
        return json.toString();
    }

    private String breadcrumbItem(int position, String name, String url) {
        return "{\"@type\":\"ListItem\",\"position\":" + position
                + ",\"name\":\"" + escapeJson(name) + "\""
                + ",\"item\":\"" + escapeJson(url) + "\"}";
    }

    private static String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            return value;
        }
    }

    /** Marker replace with a fall back to inserting before &lt;/body&gt;. */
    private String replaceMarker(String html, String marker, String content) {
        if (html.contains(marker)) {
            return html.replace(marker, content);
        }
        // Marker expected to be present. If it was removed, drop the content rather
        // than append it before </body> — these blocks must stay inside #wrapper.
        return html;
    }

    /**
     * Open Graph + Twitter Card tags. These decide how a faculty link unfurls when
     * it is pasted into WhatsApp, Messenger, Facebook, Discord, iMessage, etc. —
     * which is how these pages actually get shared, with or without an in-app share
     * button. og:image is the shared branded card (alege-facultatea-potrivita.png,
     * 1940x824, declared via og:image:width/height) so Messenger and Facebook draw
     * the large card on the first scrape. It's a static asset that always
     * resolves, so a shared link is never imageless.
     */
    private String buildSocialMetaTags(Faculty faculty, String description, String canonicalUrl, String origin,
            String countryCode) {
        String socialTitle = faculty.getFacultyName()
                + (StringUtils.isNotEmpty(faculty.getFacultyShortname()) ? " (" + faculty.getFacultyShortname() + ")" : "")
                + (StringUtils.isNotEmpty(faculty.getUniversityName()) ? " — " + faculty.getUniversityName() : "");
        String imageUrl = origin + "/assets/img/alege-facultatea-potrivita.png";
        String locale = "LT".equalsIgnoreCase(countryCode) ? "lt_LT" : "ro_RO";

        String t = escapeHtml(socialTitle);
        String d = escapeHtml(description);
        String u = escapeHtml(canonicalUrl);
        String img = escapeHtml(imageUrl);
        String alt = escapeHtml(faculty.getFacultyName());

        return "<meta property=\"og:type\" content=\"website\">\n    "
                + "<meta property=\"og:site_name\" content=\"Unistart\">\n    "
                + "<meta property=\"og:locale\" content=\"" + locale + "\">\n    "
                + "<meta property=\"og:title\" content=\"" + t + "\">\n    "
                + "<meta property=\"og:description\" content=\"" + d + "\">\n    "
                + "<meta property=\"og:url\" content=\"" + u + "\">\n    "
                + "<meta property=\"og:image\" content=\"" + img + "\">\n    "
                + "<meta property=\"og:image:type\" content=\"image/png\">\n    "
                + "<meta property=\"og:image:width\" content=\"1940\">\n    "
                + "<meta property=\"og:image:height\" content=\"824\">\n    "
                + "<meta property=\"og:image:alt\" content=\"" + alt + "\">\n    "
                + "<meta name=\"twitter:card\" content=\"summary_large_image\">\n    "
                + "<meta name=\"twitter:title\" content=\"" + t + "\">\n    "
                + "<meta name=\"twitter:description\" content=\"" + d + "\">\n    "
                + "<meta name=\"twitter:image\" content=\"" + img + "\">";
    }

    /**
     * Swaps the default OG/Twitter block in profile.html (everything between the
     * SOCIAL_META markers, markers included) for the per-faculty tags. If the
     * markers aren't found — e.g. the template drifted — it injects the tags
     * before &lt;/head&gt; instead, so the page degrades rather than silently
     * shipping the generic block.
     */
    private String replaceSocialMeta(String html, String socialTags) {
        int start = html.indexOf(SOCIAL_META_START);
        int end = html.indexOf(SOCIAL_META_END);
        if (start >= 0 && end > start) {
            return html.substring(0, start) + socialTags + html.substring(end + SOCIAL_META_END.length());
        }
        return html.replace("</head>", socialTags + "\n</head>");
    }

    /**
     * schema.org CollegeOrUniversity + AggregateRating + a short {@code review}
     * array, so eligible faculty pages can show star ratings and review snippets
     * in Google search results. aggregateRating is only included when there is at
     * least one real review — Google flags a rating with zero backing reviews as
     * invalid structured data. The reviews listed here are the same ones rendered
     * into the page body by {@link #buildTopReviewsHtml}, which Google's policy
     * requires (the marked-up reviews must be visible on the page).
     */
    private String buildJsonLd(Faculty faculty, String canonicalUrl, String origin, List<Review> topReviews) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"CollegeOrUniversity\",");
        json.append("\"name\":\"").append(escapeJson(faculty.getFacultyName())).append("\",");
        if (StringUtils.isNotEmpty(faculty.getFacultyShortname())) {
            json.append("\"alternateName\":\"").append(escapeJson(faculty.getFacultyShortname())).append("\",");
        }
        json.append("\"url\":\"").append(escapeJson(canonicalUrl)).append("\",");
        json.append("\"image\":{\"@type\":\"ImageObject\",\"url\":\"")
                .append(escapeJson(origin + "/Jamstudy/v1/faculty/" + faculty.getFacultyId() + "/cover")).append("\"}");

        if (StringUtils.isNotEmpty(faculty.getUniversityName())) {
            json.append(",\"parentOrganization\":{\"@type\":\"CollegeOrUniversity\",\"name\":\"")
                    .append(escapeJson(faculty.getUniversityName())).append("\"}");
        }

        if (StringUtils.isNotEmpty(faculty.getFacultyCity())) {
            json.append(",\"address\":{\"@type\":\"PostalAddress\",\"addressLocality\":\"")
                    .append(escapeJson(faculty.getFacultyCity())).append("\"}");
        }

        if (faculty.getAvgRating() != null && faculty.getCountRev() != null && faculty.getCountRev() > 0) {
            String ratingValue = String.format(java.util.Locale.US, "%.1f", faculty.getAvgRating());
            json.append(",\"aggregateRating\":{\"@type\":\"AggregateRating\",\"ratingValue\":\"")
                    .append(ratingValue).append("\",\"reviewCount\":\"")
                    .append(faculty.getCountRev()).append("\",\"bestRating\":\"5\",\"worstRating\":\"1\"}");
        }

        if (!topReviews.isEmpty()) {
            json.append(",\"review\":[");
            for (int i = 0; i < topReviews.size(); i++) {
                Review review = topReviews.get(i);
                if (i > 0) {
                    json.append(",");
                }
                json.append("{\"@type\":\"Review\"");
                json.append(",\"author\":{\"@type\":\"Person\",\"name\":\"")
                        .append(escapeJson(authorLabel(review))).append("\"}");
                if (review.getReviewDate() != null) {
                    json.append(",\"datePublished\":\"").append(isoDate(review.getReviewDate())).append("\"");
                }
                if (review.getGeneralRating() != null) {
                    json.append(",\"reviewRating\":{\"@type\":\"Rating\",\"ratingValue\":\"")
                            .append(review.getGeneralRating())
                            .append("\",\"bestRating\":\"5\",\"worstRating\":\"1\"}");
                }
                json.append(",\"reviewBody\":\"").append(escapeJson(reviewBody(review))).append("\"");
                json.append("}");
            }
            json.append("]");
        }

        json.append("}");
        return json.toString();
    }

    private String loadClasspathResource(String path) throws IOException {
        try (InputStream in = new ClassPathResource(path).getInputStream()) {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        }
    }

    private static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String toJsStringLiteral(String input) {
        if (input == null) {
            return "\"\"";
        }
        String escaped = input.replace("\\", "\\\\").replace("\"", "\\\"").replace("<", "\\u003C").replace(">", "\\u003E");
        return "\"" + escaped + "\"";
    }

    private static String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", " ")
                .replace("<", "\\u003C").replace(">", "\\u003E");
    }

}
