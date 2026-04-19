package com.jobportal.backend.config;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String clientRegistrationId = userRequest.getClientRegistration().getRegistrationId();

        if ("github".equals(clientRegistrationId)) {
            String email = oAuth2User.getAttribute("email");
            
            // GitHub may not return email publicly, fetch it from the /user/emails API
            if (email == null) {
                String token = userRequest.getAccessToken().getTokenValue();
                String emailUrl = "https://api.github.com/user/emails";
                
                RestTemplate restTemplate = new RestTemplate();
                HttpHeaders headers = new HttpHeaders();
                headers.add("Authorization", "Bearer " + token);
                HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
                
                ResponseEntity<Object[]> response = restTemplate.exchange(emailUrl, HttpMethod.GET, entity, Object[].class);
                if (response.getBody() != null) {
                    for (Object obj : response.getBody()) {
                        Map<String, Object> emailObj = (Map<String, Object>) obj;
                        if (Boolean.TRUE.equals(emailObj.get("primary")) && Boolean.TRUE.equals(emailObj.get("verified"))) {
                            email = (String) emailObj.get("email");
                            break;
                        }
                    }
                }
            }
            // we attach the email back to the attributes if we found it
            if (email != null) {
                java.util.Map<String, Object> attributes = new java.util.HashMap<>(oAuth2User.getAttributes());
                attributes.put("email", email);
                return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
                        oAuth2User.getAuthorities(),
                        attributes,
                        "email" // make email the primary key if possible, or login
                );
            }
        }

        return oAuth2User;
    }
}
