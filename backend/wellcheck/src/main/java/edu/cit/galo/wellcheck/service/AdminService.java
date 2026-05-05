package edu.cit.galo.wellcheck.service;

import edu.cit.galo.wellcheck.dto.AdminDashboardStats;
import edu.cit.galo.wellcheck.dto.CounselorListItem;
import edu.cit.galo.wellcheck.dto.StudentListItem;
import edu.cit.galo.wellcheck.dto.AppointmentResponse;
import edu.cit.galo.wellcheck.domain.entities.StudentProfile;
import edu.cit.galo.wellcheck.domain.entities.User;
import edu.cit.galo.wellcheck.domain.enums.UserRole;
import edu.cit.galo.wellcheck.domain.enums.UserStatus;
import edu.cit.galo.wellcheck.repository.AppointmentRepository;
import edu.cit.galo.wellcheck.repository.CounselorProfileRepository;
import edu.cit.galo.wellcheck.repository.StudentProfileRepository;
import edu.cit.galo.wellcheck.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final CounselorProfileRepository counselorProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final AppointmentRepository appointmentRepository;

    public AdminService(UserRepository userRepository,
                        CounselorProfileRepository counselorProfileRepository,
                        StudentProfileRepository studentProfileRepository,
                        AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.counselorProfileRepository = counselorProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public AdminDashboardStats getDashboardStats() {
        long totalStudents = userRepository.countByRole(UserRole.STUDENT);
        long totalCounselors = userRepository.countByRoleAndStatus(UserRole.COUNSELOR, UserStatus.ACTIVE);
        long pendingApprovals = userRepository.countByRoleAndStatus(UserRole.COUNSELOR, UserStatus.PENDING);
        long totalAppointments = appointmentRepository.count();

        return new AdminDashboardStats(totalStudents, totalCounselors, pendingApprovals, totalAppointments);
    }

    public List<CounselorListItem> getAllCounselors() {
        return counselorProfileRepository.findAll().stream()
                .map(profile -> new CounselorListItem(
                        profile.getUser().getId(),
                        profile.getUser().getFirstName(),
                        profile.getUser().getLastName(),
                        profile.getUser().getEmail(),
                        profile.getEmployeeNumber(),
                        profile.getSpecialization(),
                        profile.getUser().getStatus().name()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public String approveCounselor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Counselor not found."));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        return "Counselor approved successfully.";
    }

    @Transactional
    public String rejectCounselor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Counselor not found."));
        userRepository.delete(user);
        return "Counselor registration rejected.";
    }

    @Transactional
    public String deactivateCounselor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Counselor not found."));
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        return "Counselor account deactivated.";
    }

    public List<StudentListItem> getAllStudents() {
        return studentProfileRepository.findAll().stream()
                .map(profile -> new StudentListItem(
                        profile.getUser().getId(),
                        profile.getStudentIdNumber(),
                        profile.getUser().getFirstName(),
                        profile.getUser().getLastName(),
                        profile.getUser().getEmail(),
                        profile.getProgram(),
                        profile.getYearLevel(),
                        profile.getUser().getStatus().name(),
                        profile.getUser().getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(a -> {
                    StudentProfile sp = a.getStudent();
                    return new AppointmentResponse(
                            a.getId(),
                            a.getSlot().getId(),
                            a.getSlot().getStartTime(),
                            a.getSlot().getEndTime(),
                            a.getSlot().getCounselor().getUser().getFirstName(),
                            a.getSlot().getCounselor().getUser().getLastName(),
                            a.getSlot().getCounselor().getSpecialization(),
                            a.getSlot().getCounselor().getProfilePhoto(),
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
                            a.getRejectionReason(),
                            a.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }
}