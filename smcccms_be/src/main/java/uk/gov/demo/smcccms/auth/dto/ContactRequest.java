package uk.gov.demo.smcccms.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class ContactRequest {
    
    @NotBlank(message = "Contact information is required")
    private String contact;
    
    public ContactRequest() {}
    
    public ContactRequest(String contact) {
        this.contact = contact;
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
}