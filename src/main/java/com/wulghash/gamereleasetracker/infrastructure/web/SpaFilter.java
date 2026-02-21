package com.wulghash.gamereleasetracker.infrastructure.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Forwards all non-API, non-static-file requests to index.html
 * so React Router can handle client-side navigation (e.g. /games/123).
 *
 * A request is considered a static file if the URI contains a dot
 * (e.g. /assets/main.js, /favicon.ico). Everything else that doesn't
 * start with /api/ is forwarded to the SPA entry point.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class SpaFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (path.startsWith("/api/") || path.contains(".")) {
            filterChain.doFilter(request, response);
            return;
        }

        request.getRequestDispatcher("/index.html").forward(request, response);
    }
}
