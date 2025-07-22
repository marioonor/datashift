package com.datashift.datashift_v2.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

import com.datashift.datashift_v2.jwtutil.JwtUtil;
import com.datashift.datashift_v2.service.users.TokenBlacklistService;

import io.jsonwebtoken.ExpiredJwtException;

import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);
    @Autowired
    @Qualifier("customUserDetailsService")
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String email = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            logger.debug("JWT Token from header: {}", jwt);
            if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                logger.warn("Token is blacklisted: {}", jwt);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is blacklisted");
                return;
            }
            try {
                email = jwtUtil.extractUsername(jwt);
                logger.debug("Extracted email from token: {}", email);
            } catch (IllegalArgumentException ex) {
                logger.error("Unable to get JWT token: {}", ex.getMessage());
            } catch (ExpiredJwtException ex) {
                logger.warn("JWT token has expired: {}", ex.getMessage());
            } catch (SignatureException ex) {
                logger.warn("JWT signature does not match locally computed signature: {}", ex.getMessage());
            } catch (MalformedJwtException ex) {
                logger.warn("JWT token is malformed: {}", ex.getMessage());
            }
        } else {
            logger.debug("Authorization header missing or does not start with Bearer for request URI: {}",
                    request.getRequestURI());
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("Attempting to load UserDetails for email: {}", email);
            UserDetails userDetails;
            try {
                userDetails = this.userDetailsService.loadUserByUsername(email);
            } catch (UsernameNotFoundException e) {
                logger.warn("User '{}' found in JWT but not in the database. They may have been deleted.", email);
                // Stop processing and continue the filter chain. The user will remain
                // unauthenticated.
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtUtil.validateToken(jwt, userDetails)) {
                logger.debug("Token validated successfully for email: {}", email);
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                logger.info("Authentication set in SecurityContext for user: {}", email);
            } else {
                logger.warn("Invalid JWT token for user: {}. Token: {}", email, jwt);
            }
        }

        filterChain.doFilter(request, response);

    }
}
