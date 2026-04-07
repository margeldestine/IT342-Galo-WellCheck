package edu.cit.galo.wellcheck.observer;

import edu.cit.galo.wellcheck.entity.Appointment;
import org.springframework.stereotype.Component;

/**
 * Observer that handles email notifications when appointment status changes.
 * Currently logs to console - will be replaced with actual SMTP implementation.
 */
@Component
public class EmailNotificationObserver implements AppointmentObserver {

    @Override
    public void onStatusChanged(Appointment appointment, String oldStatus, String newStatus) {
        String studentEmail = appointment.getStudent().getUser().getEmail();
        String studentName = appointment.getStudent().getUser().getFirstName() + " " +
                appointment.getStudent().getUser().getLastName();
        String counselorName = appointment.getSlot().getCounselor().getUser().getFirstName() + " " +
                appointment.getSlot().getCounselor().getUser().getLastName();

        switch (newStatus) {
            case "CONFIRMED":
                System.out.println("\n📧 [EMAIL NOTIFICATION]");
                System.out.println("   To: " + studentEmail);
                System.out.println("   Subject: Appointment Confirmed");
                System.out.println("   Message: Hi " + studentName + ", your appointment with " +
                        counselorName + " has been confirmed.");
                System.out.println("   [SMTP implementation pending]\n");
                break;

            case "REJECTED":
                System.out.println("\n📧 [EMAIL NOTIFICATION]");
                System.out.println("   To: " + studentEmail);
                System.out.println("   Subject: Appointment Rejected");
                System.out.println("   Message: Hi " + studentName + ", unfortunately your appointment " +
                        "request has been declined.");
                System.out.println("   [SMTP implementation pending]\n");
                break;

            case "CANCELLED":
                String counselorEmail = appointment.getSlot().getCounselor().getUser().getEmail();
                System.out.println("\n📧 [EMAIL NOTIFICATION]");
                System.out.println("   To: " + counselorEmail);
                System.out.println("   Subject: Appointment Cancelled");
                System.out.println("   Message: Student " + studentName + " has cancelled their appointment.");
                System.out.println("   [SMTP implementation pending]\n");
                break;
        }
    }
}