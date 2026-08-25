package com.app.studentromania.filter;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

/**
 * The site's HTML/CSS/JS/API responses carry no Cache-Control header — only a
 * Last-Modified set by the servlet container's default static file serving (or
 * nothing at all for the dynamically-rendered pages). With no explicit directive,
 * browsers apply heuristic caching and keep serving a stale copy after a deploy
 * until the user hard-reloads. Force revalidation on every request instead: with
 * Cache-Control: no-cache the browser must check back with the server (a cheap
 * 304 when the file is unchanged) rather than silently reusing an old copy.
 *
 * Images/fonts are left alone — they rarely change on a deploy and the existing
 * heuristic caching is a reasonable default for them.
 */
@Component
public class NoCacheFilter implements Filter {

    private static final String[] CACHEABLE_EXTENSIONS = {
            ".png", ".jpg", ".jpeg", ".gif", ".svg", ".ico", ".woff", ".woff2", ".ttf", ".eot"
    };

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            String uri = ((HttpServletRequest) request).getRequestURI().toLowerCase(Locale.ROOT);
            if (!hasCacheableExtension(uri)) {
                ((HttpServletResponse) response).setHeader("Cache-Control", "no-cache");
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // no-op
    }

    private boolean hasCacheableExtension(String uri) {
        for (String extension : CACHEABLE_EXTENSIONS) {
            if (uri.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

}
