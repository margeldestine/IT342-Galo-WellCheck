package edu.cit.galo.wellcheck.observer;

import edu.cit.galo.wellcheck.entity.Appointment;
import org.springframework.stereotype.Component;

/**
 * Observer that updates dashboard statistics when appointment status changes.
 */
@Component
public class StatisticsObserver implements AppointmentObserver {

    @Override
    public void onStatusChanged(Appointment appointment, String oldStatus, String newStatus) {
        System.out.println("\n📊 [STATISTICS UPDATE]");
        System.out.println("   Appointment #" + appointment.getId());
        System.out.println("   Status Change: " + oldStatus + " → " + newStatus);
        System.out.println("   Action: Updating dashboard metrics...");
        System.out.println("   [Database update would happen here]\n");

    }
}