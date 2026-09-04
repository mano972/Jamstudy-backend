package com.app.studentromania.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.app.studentromania.dao.FacultyDAO;
import com.app.studentromania.dao.ReviewDAO;
import com.app.studentromania.model.Faculty;
import com.app.studentromania.util.RequestUtils;

/**
 * Serves a dynamically-generated sitemap.xml (every public static page plus every
 * faculty profile page that has a slug) and a matching robots.txt. Both are
 * generated per-request rather than as static files because unistart.ro and
 * unistart.lt serve different faculty data from the same codebase — a sitemap
 * needs to list only the faculties that belong to whichever domain served it,
 * and both files need to point back at the correct domain, not a hardcoded one.
 */
@Controller
public class SeoController {

    private static final String[] STATIC_PUBLIC_PAGES = {
            "", "search.html", "comparison.html", "calculator-medie-facultate.html", "articles.html", "reviews.html",
            "contact.html", "terms.html", "policy.html"
    };

    /**
     * Floor for a faculty page's <lastmod>. The Faculty model carries no per-row
     * modified timestamp, so structural edits (shortname rollouts, SSR/template
     * changes, program imports) can't be dated individually — bump this by hand
     * when such a batch lands. Per-faculty review activity is layered on top: a
     * page whose newest review is more recent than this date reports that review's
     * date instead (see {@link #sitemap}). Deliberately not "today" on every
     * request — a sitemap that always claims everything changed yesterday gets its
     * lastmod ignored.
     */
    private static final LocalDate FACULTY_CONTENT_LASTMOD = LocalDate.parse("2026-09-04");

    /**
     * <lastmod> for the static public pages. Same idea as {@link #FACULTY_CONTENT_LASTMOD}:
     * bump it when one of those pages gets a real content change (e.g. a new tool
     * page like the medie calculator, or a rewritten landing page). Without any
     * lastmod a brand-new static page sits in the sitemap with no freshness signal
     * and Google is slow to index it.
     */
    private static final String STATIC_PAGES_LASTMOD = "2026-09-04";

    private static final String[] DISALLOWED_PAGES = {
            "/login.html", "/register.html", "/forgot.html", "/change.html",
            "/admin-portal.html", "/admin-portal-login.html", "/user.html",
            "/review.html", "/ask-question.html"
    };

    @Autowired
    private FacultyDAO facultyDAO;

    @Autowired
    private ReviewDAO reviewDAO;

    @GetMapping(value = "/sitemap.xml", produces = "application/xml;charset=UTF-8")
    public void sitemap(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String origin = RequestUtils.getOrigin(request);
        String countryCode = RequestUtils.resolveCountryCode(request);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        for (String page : STATIC_PUBLIC_PAGES) {
            xml.append("  <url><loc>").append(origin).append("/").append(page)
                    .append("</loc><lastmod>").append(STATIC_PAGES_LASTMOD).append("</lastmod></url>\n");
        }

        Map<String, Date> latestReviewByFaculty = reviewDAO.getLatestReviewDateByFaculty();

        List<Faculty> faculties = facultyDAO.getAllFaculties();
        for (Faculty faculty : faculties) {
            String facultyCountry = StringUtils.isNotEmpty(faculty.getCountryCode()) ? faculty.getCountryCode() : "RO";
            if (!facultyCountry.equalsIgnoreCase(countryCode)) {
                continue;
            }
            if (StringUtils.isEmpty(faculty.getUniversitySlug()) || StringUtils.isEmpty(faculty.getFacultySlug())) {
                continue;
            }
            Date latestReview = latestReviewByFaculty.get(faculty.getFacultyId());
            // A page with reviews is never boilerplate, even with no description/programs.
            if (latestReview == null && !hasIndexableContent(faculty)) {
                continue;
            }
            xml.append("  <url><loc>").append(origin).append("/facultate/")
                    .append(faculty.getUniversitySlug()).append("/").append(faculty.getFacultySlug())
                    .append("</loc><lastmod>").append(lastmodFor(latestReview)).append("</lastmod></url>\n");
        }

        xml.append("</urlset>\n");

        response.setContentType("application/xml;charset=UTF-8");
        response.getWriter().write(xml.toString());
    }

    /**
     * Whether a faculty page carries enough unique content to be worth listing in
     * the sitemap. Every profile page is generated from one template, so a faculty
     * with no description, no programs and no reviews renders as near-boilerplate -
     * Google clusters those and reports them as "Duplicate, Google chose different
     * canonical than user". Those pages stay reachable and crawlable; they're just
     * not advertised here, so crawl budget goes to pages that can actually rank.
     */
    private boolean hasIndexableContent(Faculty faculty) {
        if (StringUtils.isNotBlank(faculty.getFacultyDescription())
                || StringUtils.isNotBlank(faculty.getFacultyPresentation())) {
            return true;
        }
        if (faculty.getCountRev() != null && faculty.getCountRev() > 0) {
            return true;
        }
        return isNotEmpty(faculty.getLicensePrograms()) || isNotEmpty(faculty.getMasterPrograms());
    }

    private static boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

    /**
     * A faculty URL's <lastmod>: the later of {@link #FACULTY_CONTENT_LASTMOD} and
     * the date of its most recent review (when it has one), as {@code yyyy-MM-dd}.
     */
    private static String lastmodFor(Date latestReview) {
        LocalDate date = FACULTY_CONTENT_LASTMOD;
        if (latestReview != null) {
            LocalDate reviewDay = latestReview.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
            if (reviewDay.isAfter(date)) {
                date = reviewDay;
            }
        }
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @GetMapping(value = "/robots.txt", produces = "text/plain;charset=UTF-8")
    public void robots(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String origin = RequestUtils.getOrigin(request);

        StringBuilder txt = new StringBuilder();
        txt.append("User-agent: *\n");
        for (String disallowed : DISALLOWED_PAGES) {
            txt.append("Disallow: ").append(disallowed).append("\n");
        }
        txt.append("\n");
        txt.append("Sitemap: ").append(origin).append("/sitemap.xml\n");

        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(txt.toString());
    }

}
