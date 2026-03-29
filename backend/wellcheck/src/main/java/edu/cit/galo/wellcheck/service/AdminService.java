package edu.cit.galo.wellcheck.service;

import edu.cit.galo.wellcheck.dto.AdminDashboardStats;
import edu.cit.galo.wellcheck.dto.CounselorListItem;
import edu.cit.galo.wellcheck.dto.StudentListItem;
import edu.cit.galo.wellcheck.entity.CounselorProfile;
import edu.cit.galo.wellcheck.entity.StudentProfile;
import edu.cit.galo.wellcheck.entity.User;
import edu.cit.galo.wellcheck.enums.UserRole;
import edu.cit.galo.wellcheck.enums.UserStatus;
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

    public AdminService(UserRepository userRepository,
                        CounselorProfileRepository counselorProfileRepository,
                        StudentProfileRepository studentProfileRepository) {
        this.userRepository = userRepository;
        this.counselorProfileRepository = counselorProfileRepository;
        this.studentProfileRepository = studentProfileRepository;
    }

    public AdminDashboardStats getDashboardStats() {
        long totalStudents = userRepository.countByRole(UserRole.STUDENT);
        long totalCounselors = userRepository.countByRoleAndStatus(UserRole.COUNSELOR, UserStatus.ACTIVE);
        long pendingApprovals = userRepository.countByRoleAndStatus(UserRole.COUNSELOR, UserStatus.PENDING);
        long totalAppointments = 0; // will update when appointments are built

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
}