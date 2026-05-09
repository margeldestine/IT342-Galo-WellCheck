package edu.cit.galo.wellcheck.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "counselor_profiles")
public class CounselorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String employeeNumber;

    @Column(nullable = false)
    private String specialization;

    @Column(columnDefinition = "TEXT")
    private String bio;

        private String profilePhoto;
        private String profilePhotoType;

    @Column
    private Integer yearsExperience;

    @Column(length = 100)
    private String licenseNumber;

    @ElementCollection
    @CollectionTable(name = "counselor_available_days",
            joinColumns = @JoinColumn(name = "counselor_profile_id"))
    @Column(name = "day")
    private List<String> availableDays = new ArrayList<>();

    public List<String> getAvailableDays() { return availableDays; }
    public void setAvailableDays(List<String> availableDays) { this.availableDays = availableDays; }

    @ElementCollection
    @CollectionTable(name = "counselor_credentials",
            joinColumns = @JoinColumn(name = "counselor_profile_id"))
    @Column(name = "credential") // each row: "Title|Year"
    private List<String> credentialEntries = new ArrayList<>();

    @Column(nullable = false)
    private double averageRating = 0.0;

    @Column(nullable = false)
    private int ratingCount = 0;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ── Getters / Setters ──────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public String getProfilePhotoType() { return profilePhotoType; }
    public void setProfilePhotoType(String profilePhotoType) { this.profilePhotoType = profilePhotoType; }

    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public List<String> getCredentialEntries() { return credentialEntries; }
    public void setCredentialEntries(List<String> credentialEntries) { this.credentialEntries = credentialEntries; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}