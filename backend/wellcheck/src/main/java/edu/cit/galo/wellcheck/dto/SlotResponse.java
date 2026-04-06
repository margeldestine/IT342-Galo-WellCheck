package edu.cit.galo.wellcheck.dto;

import java.time.LocalDateTime;

public class SlotResponse {
    private Long id;
    private Long counselorId;
    private String counselorFirstName;
    private String counselorLastName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime createdAt;

    public SlotResponse(Long id, Long counselorId, String counselorFirstName,
                        String counselorLastName, LocalDateTime startTime,
                        LocalDateTime endTime, String status, LocalDateTime createdAt) {
        this.id = id;
        this.counselorId = counselorId;
        this.counselorFirstName = counselorFirstName;
        this.counselorLastName = counselorLastName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCounselorId() { return counselorId; }
    public void setCounselorId(Long counselorId) { this.counselorId = counselorId; }
    public String getCounselorFirstName() { return counselorFirstName; }
    public void setCounselorFirstName(String counselorFirstName) { this.counselorFirstName = counselorFirstName; }
    public String getCounselorLastName() { return counselorLastName; }
    public void setCounselorLastName(String counselorLastName) { this.counselorLastName = counselorLastName; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}