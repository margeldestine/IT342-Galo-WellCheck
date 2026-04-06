package edu.cit.galo.wellcheck.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String studentIdNumber;
    @Column(nullable = false)
    private String program;
    @Column(nullable = false)
    private String yearLevel;
    @Column(nullable = false)
    private String gender;
    @Column(nullable = false)
    private LocalDate birthdate;
    @Column
    private String schoolIdPhotoUrl;
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getSchoolIdPhotoUrl() { return schoolIdPhotoUrl; }
    public void setSchoolIdPhotoUrl(String schoolIdPhotoUrl) { this.schoolIdPhotoUrl = schoolIdPhotoUrl; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}