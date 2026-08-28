package com.app.studentromania.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Small helpers for deriving per-request identity (origin domain, RO/LT country)
 * needed by controllers that render server-side content for both unistart.ro and
 * unistart.lt from the same codebase. Mirrors the client-side "Code" header logic
 * in unistart.js (window.location.href.includes("unistart.lt") ? "LT" : "RO"),
 * which only applies to the site's own AJAX calls — a crawler hitting a page
 * directly sends no such header, so server-rendered content must derive country
 * from the request's own host instead.
 */
public class RequestUtils {

    /**
     * The canonical origin for the request's domain: always https, and the host
     * normalised to one form per domain so link / crawl / ranking signals don't
     * split. Everything server-rendered — {@code <link rel="canonical">}, og:url,
     * sitemap {@code <loc>}, breadcrumb links — is built from this.
     * {@link com.app.studentromania.filter.CanonicalHostFilter} 301s the other
     * form to match.
     */
    public static String getOrigin(HttpServletRequest request) {
        return "https://" + canonicalHost(request.getServerName());
    }

    /**
     * Canonical host for a given request host:
     * <ul>
     *   <li><b>unistart.ro</b> — bare domain (a leading {@code www.} is stripped);
     *       the TLS cert and Google's selected canonical are on the bare host.</li>
     *   <li><b>unistart.lt</b> — {@code www.unistart.lt}; only that host has a TLS
     *       certificate, so it has to be the canonical one.</li>
     *   <li>anything else (e.g. localhost) — returned unchanged.</li>
     * </ul>
     */
    public static String canonicalHost(String host) {
        if (host == null || host.isEmpty()) {
            return "unistart.ro";
        }
        boolean hasWww = host.regionMatches(true, 0, "www.", 0, 4);
        if (host.toLowerCase().contains("unistart.lt")) {
            return hasWww ? host : "www." + host;
        }
        return hasWww ? host.substring(4) : host;
    }

    public static String resolveCountryCode(HttpServletRequest request) {
        String serverName = request.getServerName();
        return serverName != null && serverName.contains("unistart.lt") ? "LT" : "RO";
    }

}
