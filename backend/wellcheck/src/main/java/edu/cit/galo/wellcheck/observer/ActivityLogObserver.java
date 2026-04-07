package edu.cit.galo.wellcheck.observer;

import edu.cit.galo.wellcheck.entity.Appointment;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Observer that logs all appointment status changes for audit trail.
 */
@Component
public class ActivityLogObserver implements AppointmentObserver {

    @Override
    public void onStatusChanged(Appointment appointment, String oldStatus, String newStatus) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        System.out.println("\n📝 [ACTIVITY LOG]");
        System.out.println("   Timestamp: " + timestamp);
        System.out.println("   Appointment ID: " + appointment.getId());
        System.out.println("   Student: " + appointment.getStudent().getUser().getEmail());
        System.out.println("   Action: STATUS_CHANGED");
        System.out.println("   From: " + oldStatus + " | To: " + newStatus);

    }
}