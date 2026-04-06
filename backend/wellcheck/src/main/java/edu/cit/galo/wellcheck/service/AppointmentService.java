package edu.cit.galo.wellcheck.service;

import edu.cit.galo.wellcheck.dto.AppointmentRequest;
import edu.cit.galo.wellcheck.dto.AppointmentResponse;
import edu.cit.galo.wellcheck.entity.Appointment;
import edu.cit.galo.wellcheck.entity.Slot;
import edu.cit.galo.wellcheck.entity.StudentProfile;
import edu.cit.galo.wellcheck.enums.AppointmentStatus;
import edu.cit.galo.wellcheck.enums.SlotStatus;
import edu.cit.galo.wellcheck.repository.AppointmentRepository;
import edu.cit.galo.wellcheck.repository.SlotRepository;
import edu.cit.galo.wellcheck.repository.StudentProfileRepository;
import edu.cit.galo.wellcheck.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import edu.cit.galo.wellcheck.repository.CounselorProfileRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final SlotRepository slotRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final CounselorProfileRepository counselorProfileRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              SlotRepository slotRepository,
                              StudentProfileRepository studentProfileRepository,
                              UserRepository userRepository,
                              CounselorProfileRepository counselorProfileRepository) {
        this.appointmentRepository = appointmentRepository;
        this.slotRepository = slotRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.userRepository = userRepository;
        this.counselorProfileRepository = counselorProfileRepository;
    }

    @Transactional
    public AppointmentResponse bookAppointment(String email, AppointmentRequest request) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        StudentProfile student = studentProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found."));

        Slot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new RuntimeException("Slot not found."));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new RuntimeException("This slot is no longer available.");
        }

        boolean alreadyBooked = appointmentRepository
                .existsBySlotIdAndStatusNot(request.getSlotId(), AppointmentStatus.CANCELLED);
        if (alreadyBooked) {
            throw new RuntimeException("This slot has already been booked.");
        }

        slot.setStatus(SlotStatus.BOOKED);
        slotRepository.save(slot);

        Appointment appointment = new Appointment();
        appointment.setStudent(student);
        appointment.setSlot(slot);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setNote(request.getNote());

        Appointment saved = appointmentRepository.save(appointment);
        return toResponse(saved);
    }

    public List<AppointmentResponse> getMyAppointments(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        StudentProfile student = studentProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found."));

        return appointmentRepository.findByStudentId(student.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getCounselorAppointments(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        var counselor = counselorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Counselor profile not found."));

        return appointmentRepository.findBySlotCounselorId(counselor.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse cancelAppointment(String email, Long appointmentId) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found."));

        if (!appointment.getStudent().getUser().getEmail().equals(email)) {
            throw new RuntimeException("You are not authorized to cancel this appointment.");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new RuntimeException("Only PENDING appointments can be cancelled.");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.getSlot().setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(appointment.getSlot());
        appointmentRepository.save(appointment);

        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse approveAppointment(String email, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found."));

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new RuntimeException("Only PENDING appointments can be approved.");
        }

        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    @Transactional
    public AppointmentResponse rejectAppointment(String email, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found."));

        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new RuntimeException("Only PENDING appointments can be rejected.");
        }

        appointment.setStatus(AppointmentStatus.REJECTED);
        appointment.getSlot().setStatus(SlotStatus.AVAILABLE);
        slotRepository.save(appointment.getSlot());
        appointmentRepository.save(appointment);
        return toResponse(appointment);
    }

    private AppointmentResponse toResponse(Appointment a) {
        StudentProfile sp = a.getStudent();
        return new AppointmentResponse(
                a.getId(),
                a.getSlot().getId(),
                a.getSlot().getStartTime(),
                a.getSlot().getEndTime(),
                a.getSlot().getCounselor().getUser().getFirstName(),
                a.getSlot().getCounselor().getUser().getLastName(),
                a.getSlot().getCounselor().getSpecialization(),
                sp.getUser().getFirstName(),
                sp.getUser().getLastName(),
                sp.getStudentIdNumber(),
                sp.getProgram(),
                sp.getYearLevel(),
                sp.getGender(),
                sp.getBirthdate(),
                sp.getSchoolIdPhotoUrl(),
                a.getStatus().name(),
                a.getNote(),
                a.getCreatedAt()
        );
    }
}