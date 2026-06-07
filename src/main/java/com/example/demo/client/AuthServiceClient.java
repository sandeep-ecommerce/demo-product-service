package com.example.demo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AuthServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceClient.class);

    private final RestTemplate restTemplate;

    @Value("${auth.service.base-url}")
    private String baseUrl;

    public AuthServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Returns an AuthResult (subject + granted authorities) if the token is valid, null otherwise.
     */
    public AuthResult validateToken(String token) {
        try {
            Map<String, String> body = Map.of("token", token);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    baseUrl + "/api/v1/auth/token/validate",
                    HttpMethod.POST,
                    new HttpEntity<>(body),
                    new ParameterizedTypeReference<>() {});

            Map<String, Object> responseBody = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
                Boolean success = (Boolean) responseBody.get("success");
                if (Boolean.TRUE.equals(success)) {
                    Object data = responseBody.get("data");
                    if (data instanceof Map<?, ?> dataMap) {
                        Object sub = dataMap.get("sub");
                        String subject = sub != null ? sub.toString() : token;
                        return new AuthResult(subject, extractAuthorities(dataMap));
                    }
                    return new AuthResult(token, defaultAuthorities());
                }
            }
        } catch (Exception ex) {
            logger.warn("Token validation failed — auth service unreachable or returned an error: {}", ex.getMessage());
        }
        return null;
    }

    private List<GrantedAuthority> extractAuthorities(Map<?, ?> dataMap) {
        Object roles = dataMap.get("roles");
        if (roles instanceof List<?> roleList && !roleList.isEmpty()) {
            return roleList.stream()
                    .map(r -> {
                        String role = r.toString().toUpperCase();
                        return (GrantedAuthority) new SimpleGrantedAuthority(
                                role.startsWith("ROLE_") ? role : "ROLE_" + role);
                    })
                    .collect(Collectors.toList());
        }
        return defaultAuthorities();
    }

    private List<GrantedAuthority> defaultAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
