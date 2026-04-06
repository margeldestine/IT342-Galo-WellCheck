package edu.cit.galo.wellcheck.dto;

public class AppointmentRequest {
    private Long slotId;
    private String note;

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}