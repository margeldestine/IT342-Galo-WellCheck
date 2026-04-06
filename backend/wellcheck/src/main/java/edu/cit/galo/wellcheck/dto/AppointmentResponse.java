package edu.cit.galo.wellcheck.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AppointmentResponse {
    private Long id;
    private Long slotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String counselorFirstName;
    private String counselorLastName;
    private String counselorSpecialization;
    private String studentFirstName;
    private String studentLastName;
    private String studentIdNumber;
    private String studentProgram;
    private String studentYearLevel;
    private String studentGender;
    private LocalDate studentBirthdate;
    private String studentSchoolIdPhotoUrl;
    private String status;
    private String note;
    private LocalDateTime createdAt;

    public AppointmentResponse(Long id, Long slotId, LocalDateTime startTime,
                               LocalDateTime endTime, String counselorFirstName,
                               String counselorLastName, String counselorSpecialization,
                               String studentFirstName, String studentLastName,
                               String studentIdNumber, String studentProgram,
                               String studentYearLevel, String studentGender,
                               LocalDate studentBirthdate, String studentSchoolIdPhotoUrl,
                               String status, String note, LocalDateTime createdAt) {
        this.id = id;
        this.slotId = slotId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.counselorFirstName = counselorFirstName;
        this.counselorLastName = counselorLastName;
        this.counselorSpecialization = counselorSpecialization;
        this.studentFirstName = studentFirstName;
        this.studentLastName = studentLastName;
        this.studentIdNumber = studentIdNumber;
        this.studentProgram = studentProgram;
        this.studentYearLevel = studentYearLevel;
        this.studentGender = studentGender;
        this.studentBirthdate = studentBirthdate;
        this.studentSchoolIdPhotoUrl = studentSchoolIdPhotoUrl;
        this.status = status;
        this.note = note;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getCounselorFirstName() { return counselorFirstName; }
    public void setCounselorFirstName(String counselorFirstName) { this.counselorFirstName = counselorFirstName; }
    public String getCounselorLastName() { return counselorLastName; }
    public void setCounselorLastName(String counselorLastName) { this.counselorLastName = counselorLastName; }
    public String getCounselorSpecialization() { return counselorSpecialization; }
    public void setCounselorSpecialization(String counselorSpecialization) { this.counselorSpecialization = counselorSpecialization; }
    public String getStudentFirstName() { return studentFirstName; }
    public void setStudentFirstName(String studentFirstName) { this.studentFirstName = studentFirstName; }
    public String getStudentLastName() { return studentLastName; }
    public void setStudentLastName(String studentLastName) { this.studentLastName = studentLastName; }
    public String getStudentIdNumber() { return studentIdNumber; }
    public void setStudentIdNumber(String studentIdNumber) { this.studentIdNumber = studentIdNumber; }
    public String getStudentProgram() { return studentProgram; }
    public void setStudentProgram(String studentProgram) { this.studentProgram = studentProgram; }
    public String getStudentYearLevel() { return studentYearLevel; }
    public void setStudentYearLevel(String studentYearLevel) { this.studentYearLevel = studentYearLevel; }
    public String getStudentGender() { return studentGender; }
    public void setStudentGender(String studentGender) { this.studentGender = studentGender; }
    public java.time.LocalDate getStudentBirthdate() { return studentBirthdate; }
    public void setStudentBirthdate(java.time.LocalDate studentBirthdate) { this.studentBirthdate = studentBirthdate; }
    public String getStudentSchoolIdPhotoUrl() { return studentSchoolIdPhotoUrl; }
    public void setStudentSchoolIdPhotoUrl(String studentSchoolIdPhotoUrl) { this.studentSchoolIdPhotoUrl = studentSchoolIdPhotoUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}