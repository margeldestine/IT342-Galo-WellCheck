package edu.cit.galo.wellcheck.dto;

public class AuthResponse {
    private String accessToken;
    private String role;
    private String status;
    private String email;
    private String firstName;
    private String lastName;

    public AuthResponse(String accessToken, String role, String status, String email, String firstName, String lastName) {
        this.accessToken = accessToken;
        this.role = role;
        this.status = status;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}