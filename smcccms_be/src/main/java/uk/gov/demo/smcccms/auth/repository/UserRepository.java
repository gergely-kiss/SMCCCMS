package uk.gov.demo.smcccms.auth.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.demo.smcccms.auth.dto.UserResponse;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    private final RowMapper<UserResponse> userRowMapper = (rs, rowNum) -> {
        UserResponse user = new UserResponse();
        user.setId(rs.getLong("id"));
        user.setGovId(rs.getString("gov_id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        return user;
    };
    
    public Optional<UserResponse> findByGovId(String govId) {
        String sql = "SELECT id, gov_id, first_name, last_name FROM users WHERE gov_id = ?";
        List<UserResponse> users = jdbcTemplate.query(sql, userRowMapper, govId);
        
        if (users.isEmpty()) {
            return Optional.empty();
        }
        
        UserResponse user = users.get(0);
        user.setRoles(findUserRoles(user.getId()));
        return Optional.of(user);
    }
    
    public Optional<UserResponse> findById(Long id) {
        String sql = "SELECT id, gov_id, first_name, last_name FROM users WHERE id = ?";
        List<UserResponse> users = jdbcTemplate.query(sql, userRowMapper, id);
        
        if (users.isEmpty()) {
            return Optional.empty();
        }
        
        UserResponse user = users.get(0);
        user.setRoles(findUserRoles(user.getId()));
        return Optional.of(user);
    }
    
    @Transactional
    public Long upsertUser(String providerUserId, String govId, String firstName, String lastName) {
        String selectSql = "SELECT id FROM users WHERE gov_id = ?";
        List<Long> existingIds = jdbcTemplate.query(selectSql, (rs, rowNum) -> rs.getLong("id"), govId);
        
        if (!existingIds.isEmpty()) {
            String updateSql = "UPDATE users SET provider_user_id = ?, first_name = ?, last_name = ?, updated_at = ? WHERE id = ?";
            jdbcTemplate.update(updateSql, providerUserId, firstName, lastName, Timestamp.valueOf(LocalDateTime.now()), existingIds.get(0));
            return existingIds.get(0);
        } else {
            String insertSql = "INSERT INTO users (provider_user_id, gov_id, first_name, last_name, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
            KeyHolder keyHolder = new GeneratedKeyHolder();
            
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, providerUserId);
                ps.setString(2, govId);
                ps.setString(3, firstName);
                ps.setString(4, lastName);
                ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
                ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                return ps;
            }, keyHolder);
            
            return ((Number) keyHolder.getKeys().get("id")).longValue();
        }
    }
    
    private List<String> findUserRoles(Long userId) {
        String sql = "SELECT r.code FROM user_roles ur JOIN roles r ON ur.role_id = r.id WHERE ur.user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("code"), userId);
    }
    
    @Transactional
    public void syncUserRoles(Long userId, List<String> roleCodes) {
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", userId);
        
        for (String roleCode : roleCodes) {
            String sql = "INSERT INTO user_roles (user_id, role_id) SELECT ?, id FROM roles WHERE code = ?";
            jdbcTemplate.update(sql, userId, roleCode);
        }
    }
}