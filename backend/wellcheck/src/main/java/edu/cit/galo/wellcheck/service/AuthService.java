package edu.cit.galo.wellcheck.service;

import edu.cit.galo.wellcheck.dto.AuthResponse;
import edu.cit.galo.wellcheck.dto.CounselorRegisterRequest;
import edu.cit.galo.wellcheck.dto.LoginRequest;
import edu.cit.galo.wellcheck.dto.StudentRegisterRequest;
import edu.cit.galo.wellcheck.entity.CounselorProfile;
import edu.cit.galo.wellcheck.entity.StudentProfile;
import edu.cit.galo.wellcheck.entity.User;
import edu.cit.galo.wellcheck.enums.UserRole;
import edu.cit.galo.wellcheck.enums.UserStatus;
import edu.cit.galo.wellcheck.repository.CounselorProfileRepository;
import edu.cit.galo.wellcheck.repository.StudentProfileRepository;
import edu.cit.galo.wellcheck.repository.UserRepository;
import edu.cit.galo.wellcheck.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.core.user.OAuth2User;
import edu.cit.galo.wellcheck.dto.CompleteProfileRequest;
import edu.cit.galo.wellcheck.dto.CompleteCounselorProfileRequest;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CounselorProfileRepository counselorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       StudentProfileRepository studentProfileRepository,
                       CounselorProfileRepository counselorProfileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.counselorProfileRepository = counselorProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public String registerStudent(StudentRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered.");
        }
        if (studentProfileRepository.existsByStudentIdNumber(request.getStudentIdNumber())) {
            throw new RuntimeException("Student ID number is already registered.");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.STUDENT);
        user.setStatus(UserStatus.ACTIVE);
        user.setProfileCompleted(false);
        userRepository.save(user);

        StudentProfile profile = new StudentProfile();
        profile.setStudentIdNumber(request.getStudentIdNumber());
        profile.setProgram(request.getProgram());
        profile.setYearLevel(request.getYearLevel());
        profile.setGender(request.getGender());
        profile.setBirthdate(request.getBirthdate());
        profile.setUser(user);
        studentProfileRepository.save(profile);

        return "Student registered successfully.";
    }

    @Transactional
    public String registerCounselor(CounselorRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered.");
        }
        if (counselorProfileRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
            throw new RuntimeException("Employee number is already registered.");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.COUNSELOR);
        user.setStatus(UserStatus.PENDING);
        user.setProfileCompleted(false);
        userRepository.save(user);

        CounselorProfile profile = new CounselorProfile();
        profile.setEmployeeNumber(request.getEmployeeNumber());
        profile.setSpecialization(request.getSpecialization());
        profile.setBio(request.getBio());
        profile.setUser(user);
        counselorProfileRepository.save(profile);

        return "Counselor registration submitted. Awaiting admin approval.";
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password.");
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new RuntimeException("Your account has been deactivated.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
                token,
                user.getRole().name(),
                user.getStatus().name(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    public String getEmailFromToken(String token) {
        return jwtUtil.extractEmail(token);
    }

    public AuthResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        return new AuthResponse(
                null,
                user.getRole().name(),
                user.getStatus().name(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    @Transactional
    public String authenticateWithGoogleOAuth2User(OAuth2User oauth2User) {
        String email = oauth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Google account email is unavailable.");
        }
        final String finalEmail = email.trim().toLowerCase();

        String rawFirstName = oauth2User.getAttribute("given_name");
        String rawLastName = oauth2User.getAttribute("family_name");
        final String firstName = (rawFirstName == null || rawFirstName.isBlank()) ? "Google" : rawFirstName;
        final String lastName = (rawLastName == null || rawLastName.isBlank()) ? "User" : rawLastName;

        User user = userRepository.findByEmail(finalEmail).orElseGet(() -> {
            User newUser = new User();
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setEmail(finalEmail);
            newUser.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            newUser.setRole(UserRole.STUDENT);
            newUser.setStatus(UserStatus.ACTIVE);
            newUser.setProfileCompleted(false);
            return userRepository.save(newUser);
        });

        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }

    @Transactional
    public String completeProfile(String email, CompleteProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (studentProfileRepository.existsByStudentIdNumber(request.getStudentIdNumber())) {
            throw new RuntimeException("Student ID number is already registered.");
        }

        StudentProfile profile = new StudentProfile();
        profile.setStudentIdNumber(request.getStudentIdNumber());
        profile.setProgram(request.getProgram());
        profile.setYearLevel(request.getYearLevel());
        profile.setGender(request.getGender());
        profile.setBirthdate(request.getBirthdate());
        profile.setUser(user);
        studentProfileRepository.save(profile);

        user.setProfileCompleted(true);
        userRepository.save(user);

        return "Profile completed successfully.";
    }

    @Transactional
    public String completeCounselorProfile(String email, CompleteCounselorProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (counselorProfileRepository.existsByEmployeeNumber(request.getEmployeeNumber())) {
            throw new RuntimeException("Employee number is already registered.");
        }

        user.setRole(UserRole.COUNSELOR);
        user.setStatus(UserStatus.PENDING);
        userRepository.save(user);

        CounselorProfile profile = new CounselorProfile();
        profile.setEmployeeNumber(request.getEmployeeNumber());
        profile.setSpecialization(request.getSpecialization());
        profile.setBio(request.getBio());
        profile.setUser(user);
        counselorProfileRepository.save(profile);

        return "Counselor profile completed. Awaiting admin approval.";
    }
}