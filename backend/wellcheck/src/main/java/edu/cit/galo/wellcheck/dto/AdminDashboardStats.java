package edu.cit.galo.wellcheck.dto;

public class AdminDashboardStats {

    private long totalStudents;
    private long totalCounselors;
    private long pendingCounselorApprovals;
    private long totalAppointments;

    public AdminDashboardStats(long totalStudents, long totalCounselors,
                               long pendingCounselorApprovals, long totalAppointments) {
        this.totalStudents = totalStudents;
        this.totalCounselors = totalCounselors;
        this.pendingCounselorApprovals = pendingCounselorApprovals;
        this.totalAppointments = totalAppointments;
    }

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }
    public long getTotalCounselors() { return totalCounselors; }
    public void setTotalCounselors(long totalCounselors) { this.totalCounselors = totalCounselors; }
    public long getPendingCounselorApprovals() { return pendingCounselorApprovals; }
    public void setPendingCounselorApprovals(long pendingCounselorApprovals) { this.pendingCounselorApprovals = pendingCounselorApprovals; }
    public long getTotalAppointments() { return totalAppointments; }
    public void setTotalAppointments(long totalAppointments) { this.totalAppointments = totalAppointments; }
}