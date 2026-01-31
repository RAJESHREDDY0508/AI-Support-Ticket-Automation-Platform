package com.ticketplatform.ticketservice.filter;

import com.ticketplatform.ticketservice.config.InternalAuthConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Validates X-Internal-Token for /internal/** requests.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class InternalAuthFilter extends OncePerRequestFilter {

    public static final String HEADER_INTERNAL_TOKEN = "X-Internal-Token";

    private final InternalAuthConfig internalAuthConfig;

    public InternalAuthFilter(InternalAuthConfig internalAuthConfig) {
        this.internalAuthConfig = internalAuthConfig;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String provided = request.getHeader(HEADER_INTERNAL_TOKEN);
        if (provided == null || provided.isBlank()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Missing " + HEADER_INTERNAL_TOKEN);
            return;
        }
        if (!internalAuthConfig.isTokenValid(provided)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().write("Invalid internal token");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
