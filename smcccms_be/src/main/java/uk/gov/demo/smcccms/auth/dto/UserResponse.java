package uk.gov.demo.smcccms.auth.dto;

import java.util.List;

public class UserResponse {
    
    private Long id;
    private String govId;
    private String firstName;
    private String lastName;
    private List<String> roles;
    
    public UserResponse() {}
    
    public UserResponse(Long id, String govId, String firstName, String lastName, List<String> roles) {
        this.id = id;
        this.govId = govId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getGovId() {
        return govId;
    }
    
    public void setGovId(String govId) {
        this.govId = govId;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}