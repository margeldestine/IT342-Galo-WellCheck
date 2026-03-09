package edu.cit.galo.wellcheck.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class StudentRegisterRequest {
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;
    @NotBlank(message = "Password is required")
    private String password;
    @NotBlank(message = "Student ID number is required")
    private String studentIdNumber;
    @NotBlank(message = "Program is required")
    private String program;
    @NotBlank(message = "Year level is required")
    private String yearLevel;
    @NotBlank(message = "Gender is required")
    private String gender;
    @NotNull(message = "Birthdate is required")
    private LocalDate birthdate;

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
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