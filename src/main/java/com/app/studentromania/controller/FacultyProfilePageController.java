package com.app.studentromania.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

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
import com.app.studentromania.model.Faculty;
import com.app.studentromania.util.RequestUtils;

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

    @Autowired
    private FacultyDAO facultyDAO;

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
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(renderProfileHtml(facultyOpt.get(), universitySlug, facultySlug, RequestUtils.getOrigin(request)));
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

    private String renderProfileHtml(Faculty faculty, String universitySlug, String facultySlug, String origin) {
        String canonicalUrl = origin + "/facultate/" + universitySlug + "/" + facultySlug;
        String title = faculty.getFacultyName() + " - Unistart";
        String description = "Vezi evaluări, rating și detalii despre " + faculty.getFacultyName()
                + (StringUtils.isNotEmpty(faculty.getUniversityName()) ? " (" + faculty.getUniversityName() + ")" : "")
                + ". Alege facultatea potrivită pentru tine pe Unistart.";

        String html = profileHtmlTemplate.replace(TITLE_TAG,
                "<title>" + escapeHtml(title) + "</title>\n    <link rel=\"canonical\" href=\"" + canonicalUrl + "\">");
        html = html.replace(DESCRIPTION_TAG,
                "<meta name=\"description\" content=\"" + escapeHtml(description) + "\">");
        html = html.replace("</head>",
                "<script>window.__FACULTY_ID__ = " + toJsStringLiteral(faculty.getFacultyId()) + ";</script>\n"
                + "<script type=\"application/ld+json\">" + buildJsonLd(faculty, canonicalUrl) + "</script>\n</head>");
        return html;
    }

    /**
     * schema.org CollegeOrUniversity + AggregateRating, so eligible faculty pages
     * can show star ratings in Google search results. aggregateRating is only
     * included when there is at least one real review — Google flags a rating
     * with zero backing reviews as invalid structured data.
     */
    private String buildJsonLd(Faculty faculty, String canonicalUrl) {
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
