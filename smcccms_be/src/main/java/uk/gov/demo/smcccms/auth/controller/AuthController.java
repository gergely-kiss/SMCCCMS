package uk.gov.demo.smcccms.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.demo.smcccms.auth.dto.*;
import uk.gov.demo.smcccms.auth.repository.UserRepository;
import uk.gov.demo.smcccms.auth.service.LoginCodeService;
import uk.gov.demo.smcccms.auth.service.SessionService;
import uk.gov.demo.smcccms.auth.service.UserProviderClient;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication endpoints for government ID verification and login")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final UserProviderClient userProviderClient;
    private final UserRepository userRepository;
    private final LoginCodeService loginCodeService;
    private final SessionService sessionService;
    
    public AuthController(UserProviderClient userProviderClient,
                          UserRepository userRepository,
                          LoginCodeService loginCodeService,
                          SessionService sessionService) {
        this.userProviderClient = userProviderClient;
        this.userRepository = userRepository;
        this.loginCodeService = loginCodeService;
        this.sessionService = sessionService;
    }
    
    @PostMapping("/verify-id")
    @Operation(summary = "Verify government ID", 
               description = "Verifies a government ID with external provider and creates/updates local user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "ID verified successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid ID format"),
        @ApiResponse(responseCode = "502", description = "User provider unavailable")
    })
    public ResponseEntity<?> verifyId(@Valid @RequestBody VerifyIdRequest request) {
        try {
            logger.info("Verifying government ID: {}", request.getGovId());
            
            // Call external user provider
            UserProviderClient.ProviderUserInfo providerInfo = userProviderClient.verifyId(request.getGovId());
            
            // Upsert user in local database
            Long userId = userRepository.upsertUser(
                providerInfo.providerId, 
                request.getGovId(), 
                providerInfo.firstName, 
                providerInfo.lastName
            );
            
            // Sync roles
            userRepository.syncUserRoles(userId, providerInfo.roles);
            
            // Return normalized user object
            Optional<UserResponse> user = userRepository.findById(userId);
            if (user.isPresent()) {
                logger.info("User verified and upserted: govId={}, userId={}", request.getGovId(), userId);
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve user after upsert"));
            }
            
        } catch (Exception e) {
            logger.error("Failed to verify government ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", "User verification failed", "details", e.getMessage()));
        }
    }
    
    @PostMapping("/request-code")
    @Operation(summary = "Request verification code", 
               description = "Generates a 6-digit verification code with 24-hour TTL (demo returns code directly)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Code generated successfully",
                    content = @Content(schema = @Schema(implementation = CodeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid contact information")
    })
    public ResponseEntity<?> requestCode(@Valid @RequestBody ContactRequest request) {
        try {
            logger.info("Requesting verification code for contact: {}", request.getContact());
            
            // For demo purposes, we'll use the first user (ID-UK-001) as the default user
            // In a real implementation, we'd associate the contact with a specific user
            Optional<UserResponse> user = userRepository.findByGovId("ID-UK-001");
            if (user.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "No user found for code generation"));
            }
            
            String code = loginCodeService.generateCode(user.get().getId());
            
            logger.info("Generated verification code for user ID: {}", user.get().getId());
            
            // In demo mode, return the code directly (normally would be sent via SMS/email)
            return ResponseEntity.ok(new CodeResponse(code));
            
        } catch (Exception e) {
            logger.error("Failed to generate verification code: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Code generation failed", "details", e.getMessage()));
        }
    }
    
    @PostMapping("/verify-code")
    @Operation(summary = "Verify login code", 
               description = "Validates 6-digit code and creates authenticated session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Code verified, session created",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid code format"),
        @ApiResponse(responseCode = "401", description = "Code invalid, expired, or already used")
    })
    public ResponseEntity<?> verifyCode(@Valid @RequestBody CodeRequest request, 
                                        HttpServletResponse response) {
        try {
            logger.info("Verifying login code: {}", request.getCode());
            
            Optional<Long> userId = loginCodeService.validateAndConsumeCode(request.getCode());
            
            if (userId.isEmpty()) {
                logger.warn("Invalid or expired code: {}", request.getCode());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired verification code"));
            }
            
            // Create session
            sessionService.createSession(userId.get(), response);
            
            // Return user object
            Optional<UserResponse> user = userRepository.findById(userId.get());
            if (user.isPresent()) {
                logger.info("Code verified and session created for user ID: {}", userId.get());
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve user after code verification"));
            }
            
        } catch (Exception e) {
            logger.error("Failed to verify code: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Code verification failed", "details", e.getMessage()));
        }
    }
}