package uk.gov.demo.smcccms.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.demo.smcccms.auth.repository.LoginCodeRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginCodeServiceTest {
    
    @Mock
    private LoginCodeRepository loginCodeRepository;
    
    private LoginCodeService loginCodeService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loginCodeService = new LoginCodeService(loginCodeRepository);
    }
    
    @Test
    void generateCode_ShouldCreateSixDigitCode() {
        Long userId = 1L;
        when(loginCodeRepository.createLoginCode(eq(userId), anyString(), any(LocalDateTime.class)))
            .thenReturn(1L);
        
        String code = loginCodeService.generateCode(userId);
        
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"));
        
        verify(loginCodeRepository).createLoginCode(eq(userId), eq(code), any(LocalDateTime.class));
    }
    
    @Test
    void validateAndConsumeCode_ValidCode_ShouldReturnUserId() {
        String code = "123456";
        Long expectedUserId = 1L;
        
        when(loginCodeRepository.findUserIdByValidCode(code))
            .thenReturn(Optional.of(expectedUserId));
        
        Optional<Long> result = loginCodeService.validateAndConsumeCode(code);
        
        assertTrue(result.isPresent());
        assertEquals(expectedUserId, result.get());
        
        verify(loginCodeRepository).findUserIdByValidCode(code);
        verify(loginCodeRepository).consumeCode(code);
    }
    
    @Test
    void validateAndConsumeCode_InvalidCode_ShouldReturnEmpty() {
        String code = "123456";
        
        when(loginCodeRepository.findUserIdByValidCode(code))
            .thenReturn(Optional.empty());
        
        Optional<Long> result = loginCodeService.validateAndConsumeCode(code);
        
        assertTrue(result.isEmpty());
        
        verify(loginCodeRepository).findUserIdByValidCode(code);
        verify(loginCodeRepository, never()).consumeCode(code);
    }
    
    @Test
    void cleanupExpiredCodes_ShouldCallRepository() {
        loginCodeService.cleanupExpiredCodes();
        
        verify(loginCodeRepository).cleanupExpiredCodes();
    }
}