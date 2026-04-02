package edu.cit.galo.wellcheck.dto;

import java.time.LocalDate;

public class CompleteProfileRequest {
    private String studentIdNumber;
    private String program;
    private String yearLevel;
    private String gender;
    private LocalDate birthdate;

    public String getStudentIdNumber() { return studentIdNumber; }
    public void setStudentIdNumber(String studentIdNumber) { this.studentIdNumber = studentIdNumber; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public String getYearLevel() { return yearLevel; }
    public void setYearLevel(String yearLevel) { this.yearLevel = yearLevel; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }
}