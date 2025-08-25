package uk.gov.demo.smcccms.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerifyIdRequest {
    
    @NotBlank(message = "Government ID is required")
    @Pattern(regexp = "^ID-UK-\\d{3}$", message = "Invalid government ID format")
    private String govId;
    
    public VerifyIdRequest() {}
    
    public VerifyIdRequest(String govId) {
        this.govId = govId;
    }
    
    public String getGovId() {
        return govId;
    }
    
    public void setGovId(String govId) {
        this.govId = govId;
    }
}