package edu.cit.galo.wellcheck.observer;

import edu.cit.galo.wellcheck.entity.Appointment;

/**
 * Observer interface for appointment status changes.
 * Implements Observer Pattern to decouple notification logic.
 */
public interface AppointmentObserver {
    void onStatusChanged(Appointment appointment, String oldStatus, String newStatus);
}