package com.app.studentromania.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.app.studentromania.util.RequestUtils;

/**
 * Redirects each request to its canonical host (see
 * {@link RequestUtils#canonicalHost(String)}) with a permanent redirect, so a
 * domain's crawl / link / ranking signals don't split across host variants:
 * <ul>
 *   <li>{@code www.unistart.ro} &rarr; {@code https://unistart.ro} (bare host has
 *       the cert and is Google's selected canonical)</li>
 *   <li>{@code unistart.lt} &rarr; {@code https://www.unistart.lt} (only www has a
 *       TLS cert on .lt)</li>
 * </ul>
 *
 * Only the host is rewritten — path and query are preserved and the target is
 * always https (the site is https-only; behind a TLS-terminating proxy
 * {@code request.getScheme()} may report http). http-&gt;https itself is left to
 * the edge, the only layer that knows the real scheme.
 *
 * Runs before {@link NoCacheFilter} so redirected requests skip the rest of the
 * chain.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CanonicalHostFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String host = httpRequest.getServerName();
            String canonical = RequestUtils.canonicalHost(host);

            if (host != null && !host.equalsIgnoreCase(canonical)) {
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                String uri = httpRequest.getRequestURI();
                String query = httpRequest.getQueryString();
                String target = "https://" + canonical + uri + (query != null ? "?" + query : "");

                httpResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                httpResponse.setHeader("Location", target);
                httpResponse.setHeader("Cache-Control", "max-age=3600");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // no-op
    }
}
