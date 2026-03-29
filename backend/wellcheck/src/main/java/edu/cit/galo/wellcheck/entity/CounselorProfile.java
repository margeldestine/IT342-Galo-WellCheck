package edu.cit.galo.wellcheck.entity;

import jakarta.persistence.*;

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

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

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
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}