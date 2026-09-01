package com.app.studentromania.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.app.studentromania.dao.FacultyDAO;
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

    private static final String[] DISALLOWED_PAGES = {
            "/login.html", "/register.html", "/forgot.html", "/change.html",
            "/admin-portal.html", "/admin-portal-login.html", "/user.html",
            "/review.html", "/ask-question.html"
    };

    @Autowired
    private FacultyDAO facultyDAO;

    @GetMapping(value = "/sitemap.xml", produces = "application/xml;charset=UTF-8")
    public void sitemap(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String origin = RequestUtils.getOrigin(request);
        String countryCode = RequestUtils.resolveCountryCode(request);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        for (String page : STATIC_PUBLIC_PAGES) {
            xml.append("  <url><loc>").append(origin).append("/").append(page).append("</loc></url>\n");
        }

        List<Faculty> faculties = facultyDAO.getAllFaculties();
        for (Faculty faculty : faculties) {
            String facultyCountry = StringUtils.isNotEmpty(faculty.getCountryCode()) ? faculty.getCountryCode() : "RO";
            if (!facultyCountry.equalsIgnoreCase(countryCode)) {
                continue;
            }
            if (StringUtils.isEmpty(faculty.getUniversitySlug()) || StringUtils.isEmpty(faculty.getFacultySlug())) {
                continue;
            }
            xml.append("  <url><loc>").append(origin).append("/facultate/")
                    .append(faculty.getUniversitySlug()).append("/").append(faculty.getFacultySlug())
                    .append("</loc></url>\n");
        }

        xml.append("</urlset>\n");

        response.setContentType("application/xml;charset=UTF-8");
        response.getWriter().write(xml.toString());
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
