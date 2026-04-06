package edu.cit.galo.wellcheck.repository;

import edu.cit.galo.wellcheck.entity.Appointment;
import edu.cit.galo.wellcheck.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByStudentId(Long studentId);
    List<Appointment> findBySlotCounselorId(Long counselorId);
    List<Appointment> findByStudentIdAndStatus(Long studentId, AppointmentStatus status);
    boolean existsBySlotIdAndStatusNot(Long slotId, AppointmentStatus status);
}