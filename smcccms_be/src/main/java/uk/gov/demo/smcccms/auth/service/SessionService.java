package uk.gov.demo.smcccms.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {
    
    private static final String COOKIE_NAME = "SMCCCMS_SESSION";
    private static final String ALGORITHM = "HmacSHA256";
    
    private final String sessionSecret;
    private final Map<String, Long> activeSessions = new ConcurrentHashMap<>();
    
    public SessionService(@Value("${session.secret:smcccms-demo-secret-key}") String sessionSecret) {
        this.sessionSecret = sessionSecret;
    }
    
    public void createSession(Long userId, HttpServletResponse response) {
        String sessionId = generateSessionId(userId);
        activeSessions.put(sessionId, userId);
        
        Cookie cookie = new Cookie(COOKIE_NAME, sessionId);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/api");
        cookie.setMaxAge(24 * 60 * 60); // 24 hours
        
        response.addCookie(cookie);
    }
    
    public boolean isValidSession(String sessionId) {
        return activeSessions.containsKey(sessionId) && verifySignature(sessionId);
    }
    
    public Long getUserIdFromSession(String sessionId) {
        return activeSessions.get(sessionId);
    }
    
    public void invalidateSession(String sessionId) {
        activeSessions.remove(sessionId);
    }
    
    private String generateSessionId(Long userId) {
        String data = userId.toString() + ":" + System.currentTimeMillis();
        String signature = sign(data);
        return Base64.getUrlEncoder().encodeToString((data + ":" + signature).getBytes(StandardCharsets.UTF_8));
    }
    
    private boolean verifySignature(String sessionId) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(sessionId), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            
            if (parts.length != 3) {
                return false;
            }
            
            String data = parts[0] + ":" + parts[1];
            String signature = parts[2];
            String expectedSignature = sign(data);
            
            return signature.equals(expectedSignature);
        } catch (Exception e) {
            return false;
        }
    }
    
    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(sessionSecret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            mac.init(keySpec);
            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to sign session data", e);
        }
    }
}