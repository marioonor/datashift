package com.datashift.datashift_v2.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only log for the endpoint we are debugging
        if ("/api/extract".equals(request.getRequestURI())) {
            log.info(">>>>>>>>> Logging headers for /api/extract <<<<<<<<<");
            Collections.list(request.getHeaderNames()).forEach(headerName -> 
                log.info("{}: {}", headerName, request.getHeader(headerName))
            );
            log.info(">>>>>>>>> End of headers <<<<<<<<<");
        }

        filterChain.doFilter(request, response);
    }
}
