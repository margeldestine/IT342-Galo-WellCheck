package edu.cit.galo.wellcheck.features.appointments;

import edu.cit.galo.wellcheck.domain.entities.Appointment;
import edu.cit.galo.wellcheck.domain.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByStudentId(Long studentId);
    List<Appointment> findBySlotCounselorId(Long counselorId);
    List<Appointment> findByStudentIdAndStatus(Long studentId, AppointmentStatus status);
    boolean existsBySlotIdAndStatusNot(Long slotId, AppointmentStatus status);

    // Count appointments for a specific slot (for delete validation)
    long countBySlotId(Long slotId);
}