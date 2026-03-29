package edu.cit.galo.wellcheck.dto;

import java.time.LocalDateTime;

public class StudentListItem {

    private Long id;
    private String studentIdNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String program;
    private String yearLevel;
    private String status;
    private LocalDateTime createdAt;

    public StudentListItem(Long id, String studentIdNumber, String firstName,
                           String lastName, String email, String program,
                           String yearLevel, String status, LocalDateTime createdAt) {
        this.id = id;
        this.studentIdNumber = studentIdNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.program = program;
        this.yearLevel = yearLevel;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentIdNumber() { return studentIdNumber; }
    public void setStudentIdNumber(String studentIdNumber) { this.studentIdNumber = studentIdNumber; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public String getYearLevel() { return yearLevel; }
    public void setYearLevel(String yearLevel) { this.yearLevel = yearLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}