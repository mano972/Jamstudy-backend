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

    public static String getOrigin(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName();
    }

    public static String resolveCountryCode(HttpServletRequest request) {
        String serverName = request.getServerName();
        return serverName != null && serverName.contains("unistart.lt") ? "LT" : "RO";
    }

}
