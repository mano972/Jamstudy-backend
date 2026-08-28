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
        String title = faculty.getFacultyName()
                + (StringUtils.isNotEmpty(faculty.getUniversityName()) ? " (" + faculty.getUniversityName() + ")" : "")
                + " — păreri și evaluări de la studenți | Unistart";
        String description = "Vezi evaluări, rating și detalii despre " + faculty.getFacultyName()
                + (StringUtils.isNotEmpty(faculty.getUniversityName()) ? " (" + faculty.getUniversityName() + ")" : "")
                + ". Alege facultatea potrivită pentru tine pe Unistart.";

        String html = profileHtmlTemplate.replace(TITLE_TAG,
                "<title>" + escapeHtml(title) + "</title>\n    <link rel=\"canonical\" href=\"" + canonicalUrl + "\">");
        html = html.replace(DESCRIPTION_TAG,
                "<meta name=\"description\" content=\"" + escapeHtml(description) + "\">");
        html = replaceSocialMeta(html, buildSocialMetaTags(faculty, description, canonicalUrl, origin, countryCode));
        html = replaceTopReviews(html, buildTopReviewsHtml(faculty, topReviews));
        html = replaceMarker(html, RELATED_MARKER, buildRelatedFacultiesHtml(faculty, countryCode));
        html = html.replace("</head>",
                "<script>window.__FACULTY_ID__ = " + toJsStringLiteral(faculty.getFacultyId()) + ";</script>\n"
                + "<script type=\"application/ld+json\">" + buildJsonLd(faculty, canonicalUrl, topReviews) + "</script>\n</head>");
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
        List<Faculty> sameDomain = sameDomainFaculties(faculty, countryCode, shown);

        if (sameUni.isEmpty() && sameDomain.isEmpty()) {
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
                    .append("\">").append(escapeHtml(f.getFacultyName())).append("</a>");
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

    private String primaryDomain(Faculty faculty) {
        List<String> domains = faculty.getFacultyDomainsLicense();
        if (domains == null || domains.isEmpty()) {
            return null;
        }
        return StringUtils.isNotBlank(domains.get(0)) ? domains.get(0).trim() : null;
    }

    private static boolean hasSlug(Faculty f) {
        return StringUtils.isNotEmpty(f.getUniversitySlug()) && StringUtils.isNotEmpty(f.getFacultySlug());
    }

    private static String nvl(String value, String fallback) {
        return StringUtils.isNotBlank(value) ? value : fallback;
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
     * button. og:image points at the faculty cover endpoint, which always resolves
     * (it falls back to a default cover), so a shared link is never imageless.
     */
    private String buildSocialMetaTags(Faculty faculty, String description, String canonicalUrl, String origin,
            String countryCode) {
        String socialTitle = faculty.getFacultyName()
                + (StringUtils.isNotEmpty(faculty.getUniversityName()) ? " — " + faculty.getUniversityName() : "");
        String imageUrl = origin + "/Jamstudy/v1/faculty/" + faculty.getFacultyId() + "/cover";
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
    private String buildJsonLd(Faculty faculty, String canonicalUrl, List<Review> topReviews) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"@context\":\"https://schema.org\",");
        json.append("\"@type\":\"CollegeOrUniversity\",");
        json.append("\"name\":\"").append(escapeJson(faculty.getFacultyName())).append("\",");
        json.append("\"url\":\"").append(escapeJson(canonicalUrl)).append("\"");

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
