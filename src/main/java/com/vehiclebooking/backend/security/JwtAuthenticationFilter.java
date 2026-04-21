package com.vehiclebooking.backend.security;

import com.vehiclebooking.backend.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.GrantedAuthority; // Added import
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Added import
import io.jsonwebtoken.Claims; // Added import

import java.io.IOException;
import java.util.Collection; // Added import
import java.util.List; // Added import
import java.util.stream.Collectors; // Added import

import org.slf4j.Logger; // Added
import org.slf4j.LoggerFactory; // Added

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class); // Added

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        log.info("Request URI: {}", request.getRequestURI());
        java.util.Collections.list(request.getHeaderNames())
            .forEach(headerName -> 
                log.info("Header: {} = {}", headerName, request.getHeader(headerName))
            );

        if (request.getRequestURI().startsWith("/api/auth")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Log all headers for debugging
        java.util.Collections.list(request.getHeaderNames())
            .forEach(headerName -> 
                log.info("Header: {} = {}", headerName, request.getHeader(headerName))
            );

        log.info("JwtAuthenticationFilter: Initial authentication: {}", SecurityContextHolder.getContext().getAuthentication());
        log.info("JwtAuthenticationFilter: Processing request to {}", request.getRequestURI());
        try {
            final String authHeader = request.getHeader("Authorization");
            final String jwt;
            final String userEmail;

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.info("JwtAuthenticationFilter: No JWT token found in request headers.");
                filterChain.doFilter(request, response);
                return;
            }

            jwt = authHeader.substring(7);
            userEmail = jwtUtils.extractUsername(jwt);
            log.info("JwtAuthenticationFilter: Extracted username '{}' from JWT.", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                if (jwtUtils.validateToken(jwt, userDetails)) {
                    log.info("JwtAuthenticationFilter: JWT token is valid.");
                    // Extract authorities from JWT "roles" claim instead of re-fetching from DB
                    Claims claims = jwtUtils.extractAllClaims(jwt);
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) claims.get("roles");
                    
                    Collection<? extends GrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    System.out.println("Authorities from JWT: " + authorities);
                    log.info("User authorities: {}", authorities); // Added this line

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication( authToken);
                    log.info("JwtAuthenticationFilter: User '{}' authenticated successfully.", userEmail);
                } else {
                    log.warn("JwtAuthenticationFilter: JWT token is invalid.");
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("JwtAuthenticationFilter: An error occurred during JWT authentication.", e);
            // We are not sending a response here, just logging the error.
            // The exception will be propagated up the filter chain.
            throw e;
        }
    }
}
