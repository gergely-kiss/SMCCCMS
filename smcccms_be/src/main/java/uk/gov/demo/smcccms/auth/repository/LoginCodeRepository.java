package uk.gov.demo.smcccms.auth.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class LoginCodeRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public LoginCodeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Transactional
    public Long createLoginCode(Long userId, String code, LocalDateTime expiresAt) {
        String sql = "INSERT INTO login_codes (user_id, code, expires_at) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, code);
            ps.setTimestamp(3, Timestamp.valueOf(expiresAt));
            return ps;
        }, keyHolder);
        
        return ((Number) keyHolder.getKeys().get("id")).longValue();
    }
    
    public Optional<Long> findUserIdByValidCode(String code) {
        String sql = "SELECT user_id FROM login_codes WHERE code = ? AND expires_at > ? AND consumed_at IS NULL";
        
        var results = jdbcTemplate.query(sql, 
            (rs, rowNum) -> rs.getLong("user_id"),
            code, Timestamp.valueOf(LocalDateTime.now())
        );
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    @Transactional
    public void consumeCode(String code) {
        String sql = "UPDATE login_codes SET consumed_at = ? WHERE code = ? AND consumed_at IS NULL";
        jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()), code);
    }
    
    @Transactional
    public void cleanupExpiredCodes() {
        String sql = "DELETE FROM login_codes WHERE expires_at < ?";
        jdbcTemplate.update(sql, Timestamp.valueOf(LocalDateTime.now()));
    }
}