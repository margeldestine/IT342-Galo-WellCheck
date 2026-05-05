package edu.cit.galo.wellcheck.observer;

import edu.cit.galo.wellcheck.domain.entities.Appointment;
import edu.cit.galo.wellcheck.features.appointments.AppointmentObserver;
import edu.cit.galo.wellcheck.service.EmailService;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class EmailNotificationObserver implements AppointmentObserver {

    private final EmailService emailService;

    public EmailNotificationObserver(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public void onStatusChanged(Appointment appointment, String oldStatus, String newStatus) {
        String studentEmail  = appointment.getStudent().getUser().getEmail();
        String studentName   = appointment.getStudent().getUser().getFirstName();
        String counselorName = "Dr. " + appointment.getSlot().getCounselor().getUser().getFirstName()
                + " " + appointment.getSlot().getCounselor().getUser().getLastName();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a");
        String dateTime = appointment.getSlot().getStartTime().format(formatter);

        if ("CONFIRMED".equals(newStatus)) {
            String subject = "Your Appointment is Confirmed – WellCheck";
            String body = String.format(
                    "Hi %s,\n\n" +
                            "Great news! Your counseling appointment has been confirmed.\n\n" +
                            "─────────────────────────────\n" +
                            "  APPOINTMENT DETAILS\n" +
                            "─────────────────────────────\n" +
                            "  Counselor : %s\n" +
                            "  Date & Time: %s\n" +
                            "─────────────────────────────\n\n" +
                            "A few reminders before your session:\n" +
                            "  • Please be present at least 5 minutes early.\n" +
                            "  • If you need to cancel, please do so as soon as possible\n" +
                            "    so the slot can be opened for others.\n" +
                            "  • You can manage your appointments anytime at WellCheck.\n\n" +
                            "We're here for you. Take care of yourself.\n\n" +
                            "Warm regards,\n" +
                            "The WellCheck Team\n" +
                            "─────────────────────────────\n" +
                            "This is an automated message. Please do not reply to this email.",
                    studentName, counselorName, dateTime
            );
            emailService.sendEmail(studentEmail, subject, body);

        } else if ("REJECTED".equals(newStatus)) {
            String reason = appointment.getRejectionReason() != null
                    && !appointment.getRejectionReason().isBlank()
                    ? appointment.getRejectionReason()
                    : "No specific reason was provided.";

            String subject = "Update on Your Appointment Request – WellCheck";
            String body = String.format(
                    "Hi %s,\n\n" +
                            "Thank you for reaching out through WellCheck. We want to let you\n" +
                            "know that your appointment request could not be approved at this time.\n\n" +
                            "─────────────────────────────\n" +
                            "  APPOINTMENT DETAILS\n" +
                            "─────────────────────────────\n" +
                            "  Counselor    : %s\n" +
                            "  Requested Time: %s\n" +
                            "  Reason        : %s\n" +
                            "─────────────────────────────\n\n" +
                            "This doesn't mean you can't get support. Here's what you can do:\n" +
                            "  • Browse available counselors and book a different slot.\n" +
                            "  • Try selecting a different date or time that works for you.\n\n" +
                            "Remember, seeking help is a sign of strength. We're always here.\n\n" +
                            "Warm regards,\n" +
                            "The WellCheck Team\n" +
                            "─────────────────────────────\n" +
                            "This is an automated message. Please do not reply to this email.",
                    studentName, counselorName, dateTime, reason
            );
            emailService.sendEmail(studentEmail, subject, body);
        }
    }
}