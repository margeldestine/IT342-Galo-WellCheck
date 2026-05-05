package edu.cit.galo.wellcheck.dto;

import edu.cit.galo.wellcheck.features.auth.CredentialItem;

import java.util.List;

public class CompleteCounselorProfileRequest {
    private String employeeNumber;
    private String specialization;
    private String bio;

    private Integer yearsExperience;
    private String licenseNumber;
    private List<CredentialItem> credentials;
    private List<String> availableDays;

    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public List<CredentialItem> getCredentials() { return credentials; }
    public void setCredentials(List<CredentialItem> credentials) { this.credentials = credentials; }
    public List<String> getAvailableDays() { return availableDays; }
    public void setAvailableDays(List<String> availableDays) { this.availableDays = availableDays; }
}