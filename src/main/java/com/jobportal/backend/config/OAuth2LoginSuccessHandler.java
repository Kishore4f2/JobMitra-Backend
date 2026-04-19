package com.jobportal.backend.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.jobportal.backend.model.User;
import com.jobportal.backend.repository.UserRepository;
import com.jobportal.backend.util.JwtUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Fallback for github name if empty
        if (name == null) {
            name = oAuth2User.getAttribute("login");
        }
        
        if (email == null) {
            // If email is still null, we cannot proceed securely
            response.sendRedirect(frontendUrl + "/login?error=OAuth2EmailNotFound");
            return;
        }

        User user = userRepository.findByEmail(email);

        if (user == null) {
            // Register as Job Seeker ONLY
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setRole("SEEKER"); // Assign jobseeker role only
            user.setStatus("active");
            user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString())); // Random password
            userRepository.save(user);
            user = userRepository.findByEmail(email); // Fetch to get the generated ID
        } else {
            // If the user already exists but role is not seeker, we might want to allow them
            // or reject them. The prompt says "integrate google and github authentication for jobseeker only".
             if (!"SEEKER".equalsIgnoreCase(user.getRole())) {
                 response.sendRedirect(frontendUrl + "/login?error=OAuth2AllowedForJobSeekerOnly");
                 return;
             }
        }

        String token = JwtUtil.generateToken(user.getEmail(), user.getRole());

        // We redirect back to frontend with the token and user details in the query parameters
        // The frontend should capture these from the URL, save them to state/localstorage, and clear the URL
        String targetUrl = frontendUrl + "/oauth2/redirect?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                + "&id=" + user.getId()
                + "&name=" + URLEncoder.encode(user.getName(), StandardCharsets.UTF_8)
                + "&email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
                + "&role=" + URLEncoder.encode(user.getRole(), StandardCharsets.UTF_8);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
