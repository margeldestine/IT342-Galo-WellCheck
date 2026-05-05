package edu.cit.galo.wellcheck.features.auth;

public class AuthResponse {
    private String accessToken;
    private String role;
    private String status;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePhoto;
    private String specialization;

    public AuthResponse(String accessToken, String role, String status,
                        String email, String firstName, String lastName,
                        String profilePhoto, String specialization) {
        this.accessToken = accessToken;
        this.role = role;
        this.status = status;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePhoto = profilePhoto;
        this.specialization = specialization;
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
    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
}