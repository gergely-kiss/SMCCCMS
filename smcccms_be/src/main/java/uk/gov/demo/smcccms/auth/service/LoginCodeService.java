package uk.gov.demo.smcccms.auth.service;

import org.springframework.stereotype.Service;
import uk.gov.demo.smcccms.auth.repository.LoginCodeRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class LoginCodeService {
    
    private final LoginCodeRepository loginCodeRepository;
    private final SecureRandom random;
    
    public LoginCodeService(LoginCodeRepository loginCodeRepository) {
        this.loginCodeRepository = loginCodeRepository;
        this.random = new SecureRandom();
    }
    
    public String generateCode(Long userId) {
        String code = generateSixDigitCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
        
        loginCodeRepository.createLoginCode(userId, code, expiresAt);
        
        return code;
    }
    
    public Optional<Long> validateAndConsumeCode(String code) {
        Optional<Long> userId = loginCodeRepository.findUserIdByValidCode(code);
        
        if (userId.isPresent()) {
            loginCodeRepository.consumeCode(code);
        }
        
        return userId;
    }
    
    private String generateSixDigitCode() {
        return String.format("%06d", random.nextInt(1000000));
    }
    
    public void cleanupExpiredCodes() {
        loginCodeRepository.cleanupExpiredCodes();
    }
}