package edu.cit.galo.wellcheck.features.counselors;

import edu.cit.galo.wellcheck.dto.CredentialItem;

import java.util.List;

public class CounselorListItem {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String employeeNumber;
    private String specialization;
    private String status;
    private String bio;
    private int availableSlots;
    private Integer yearsExperience;
    private String licenseNumber;
    private double averageRating;
    private int ratingCount;
    private List<CredentialItem> credentials;
    private String profilePhoto;

    public CounselorListItem(Long id, String firstName, String lastName,
                             String email, String employeeNumber,
                             String specialization, String status,
                             String bio, int availableSlots,
                             Integer yearsExperience, String licenseNumber,
                             double averageRating, int ratingCount,
                             List<CredentialItem> credentials, String profilePhoto) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.employeeNumber = employeeNumber;
        this.specialization = specialization;
        this.status = status;
        this.bio = bio;
        this.availableSlots = availableSlots;
        this.yearsExperience = yearsExperience;
        this.licenseNumber = licenseNumber;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
        this.credentials = credentials;
        this.profilePhoto = profilePhoto;
    }

    // Slim constructor (for AdminService — admin-facing, no new fields needed)
    public CounselorListItem(Long id, String firstName, String lastName,
                             String email, String employeeNumber,
                             String specialization, String status) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.employeeNumber = employeeNumber;
        this.specialization = specialization;
        this.status = status;
    }

    public CounselorListItem(Long id, String firstName, String lastName,
                             String email, String employeeNumber,
                             String specialization, String status,
                             String bio, int availableSlots) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.employeeNumber = employeeNumber;
        this.specialization = specialization;
        this.status = status;
        this.bio = bio;
        this.availableSlots = availableSlots;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public int getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(int availableSlots) { this.availableSlots = availableSlots; }
    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
    public List<CredentialItem> getCredentials() { return credentials; }
    public void setCredentials(List<CredentialItem> credentials) { this.credentials = credentials; }
    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
}