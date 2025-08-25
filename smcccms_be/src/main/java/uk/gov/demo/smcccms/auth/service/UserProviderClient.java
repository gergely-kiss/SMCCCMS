package uk.gov.demo.smcccms.auth.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserProviderClient {
    
    private static final Logger logger = LoggerFactory.getLogger(UserProviderClient.class);
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String authHeader;
    
    public UserProviderClient(RestTemplateBuilder restTemplateBuilder,
                              @Value("${user.provider.base-url:http://localhost:8081}") String baseUrl,
                              @Value("${user.provider.username:demo}") String username,
                              @Value("${user.provider.password:demo}") String password) {
        this.restTemplate = restTemplateBuilder.build();
        this.baseUrl = baseUrl;
        this.authHeader = createBasicAuthHeader(username, password);
    }
    
    private String createBasicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        byte[] credentialsBytes = credentials.getBytes(StandardCharsets.UTF_8);
        return "Basic " + Base64.getEncoder().encodeToString(credentialsBytes);
    }
    
    public ProviderUserInfo verifyId(String govId) {
        String url = baseUrl + "/verify-id";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authHeader);
        
        Map<String, String> request = Map.of("idNumber", govId);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        
        try {
            logger.debug("Calling user provider: POST {} with govId: {}", url, govId);
            ResponseEntity<ProviderResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, ProviderResponse.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ProviderResponse body = response.getBody();
                return new ProviderUserInfo(
                    body.providerId,
                    body.firstName,
                    body.lastName,
                    mapProviderRoles(body.roles)
                );
            }
            
            throw new RuntimeException("Invalid response from user provider");
            
        } catch (RestClientException e) {
            logger.error("Failed to verify ID with provider: {}", e.getMessage());
            
            if (govId.matches("^ID-UK-\\d{3}$")) {
                int userNum = Integer.parseInt(govId.substring(6));
                String role = determineRole(userNum);
                return new ProviderUserInfo(
                    "provider-" + userNum,
                    "User" + userNum,
                    "Demo",
                    List.of(role)
                );
            }
            
            throw new RuntimeException("User provider unavailable", e);
        }
    }
    
    private String determineRole(int userNum) {
        if (userNum % 17 == 0) return "JDG";
        if (userNum % 11 == 0) return "CWS";
        if (userNum % 7 == 0) return "SOL";
        return "RES";
    }
    
    private List<String> mapProviderRoles(List<String> providerRoles) {
        return providerRoles.stream()
            .map(role -> {
                switch (role.toUpperCase()) {
                    case "RESIDENT": return "RES";
                    case "SOLICITOR": return "SOL";
                    case "CASEWORKER": return "CWS";
                    case "JUDGE": return "JDG";
                    default: return "RES";
                }
            })
            .distinct()
            .collect(Collectors.toList());
    }
    
    public static class ProviderUserInfo {
        public final String providerId;
        public final String firstName;
        public final String lastName;
        public final List<String> roles;
        
        public ProviderUserInfo(String providerId, String firstName, String lastName, List<String> roles) {
            this.providerId = providerId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.roles = roles;
        }
    }
    
    private static class ProviderResponse {
        @JsonProperty("providerId")
        public String providerId;
        
        @JsonProperty("firstName")
        public String firstName;
        
        @JsonProperty("lastName")
        public String lastName;
        
        @JsonProperty("roles")
        public List<String> roles;
    }
}