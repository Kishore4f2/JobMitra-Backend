package com.jobportal.backend.config;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import java.io.IOException;
import java.util.ArrayList;

import org.springframework.web.filter.OncePerRequestFilter;

import com.jobportal.backend.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            try {
                String email = JwtUtil.extractEmail(token);
                String role = JwtUtil.extractRole(token);

                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    java.util.List<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>();
                    if (role != null && !role.trim().isEmpty()) {
                        String cleanRole = role.trim().toUpperCase().replace("ROLE_", "").trim();
                        String finalRole = "ROLE_" + cleanRole;
                        authorities
                                .add(new org.springframework.security.core.authority.SimpleGrantedAuthority(finalRole));
                        System.out.println(
                                "DEBUG >>> JWT Filter: Authenticated " + email + " with authority: " + finalRole);
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            email, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // Spring security will handle access denied
            }
        }

        filterChain.doFilter(request, response);
    }
}
