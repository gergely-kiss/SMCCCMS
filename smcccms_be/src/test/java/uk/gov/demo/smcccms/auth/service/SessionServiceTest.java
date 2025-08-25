package uk.gov.demo.smcccms.auth.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionServiceTest {
    
    @Mock
    private HttpServletResponse response;
    
    private SessionService sessionService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sessionService = new SessionService("test-secret-key");
    }
    
    @Test
    void createSession_ShouldSetCookieWithValidSignature() {
        Long userId = 1L;
        
        sessionService.createSession(userId, response);
        
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        
        Cookie cookie = cookieCaptor.getValue();
        assertEquals("SMCCCMS_SESSION", cookie.getName());
        assertEquals("/api", cookie.getPath());
        assertEquals(24 * 60 * 60, cookie.getMaxAge());
        
        // Verify the session ID is valid
        String sessionId = cookie.getValue();
        assertTrue(sessionService.isValidSession(sessionId));
        assertEquals(userId, sessionService.getUserIdFromSession(sessionId));
    }
    
    @Test
    void isValidSession_InvalidSignature_ShouldReturnFalse() {
        String invalidSessionId = "invalid-session";
        
        assertFalse(sessionService.isValidSession(invalidSessionId));
    }
    
    @Test
    void invalidateSession_ShouldRemoveSession() {
        Long userId = 1L;
        sessionService.createSession(userId, response);
        
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(response).addCookie(cookieCaptor.capture());
        
        String sessionId = cookieCaptor.getValue().getValue();
        assertTrue(sessionService.isValidSession(sessionId));
        
        sessionService.invalidateSession(sessionId);
        assertFalse(sessionService.isValidSession(sessionId));
    }
}