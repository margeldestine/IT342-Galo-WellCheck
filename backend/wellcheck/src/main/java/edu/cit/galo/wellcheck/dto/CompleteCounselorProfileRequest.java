package edu.cit.galo.wellcheck.dto;

public class CompleteCounselorProfileRequest {
    private String employeeNumber;
    private String specialization;
    private String bio;

    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}