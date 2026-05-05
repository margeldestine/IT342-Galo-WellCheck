package edu.cit.galo.wellcheck.service;

import edu.cit.galo.wellcheck.dto.AuthResponse;
import edu.cit.galo.wellcheck.features.counselors.CounselorRegisterRequest;
import edu.cit.galo.wellcheck.dto.CredentialItem;
import edu.cit.galo.wellcheck.dto.LoginRequest;
import edu.cit.galo.wellcheck.features.students.StudentRegisterRequest;
import edu.cit.galo.wellcheck.domain.entities.CounselorProfile;
import edu.cit.galo.wellcheck.domain.entities.StudentProfile;
import edu.cit.galo.wellcheck.domain.entities.User;
import edu.cit.galo.wellcheck.domain.enums.UserRole;
import edu.cit.galo.wellcheck.domain.enums.UserStatus;
import edu.cit.galo.wellcheck.factory.CounselorProfileFactory;
import edu.cit.galo.wellcheck.features.students.StudentProfileFactory;
import edu.cit.galo.wellcheck.features.counselors.CounselorProfileRepository;
import edu.cit.galo.wellcheck.features.students.StudentProfileRepository;
import edu.cit.galo.wellcheck.repository.UserRepository;
import edu.cit.galo.wellcheck.core.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import edu.cit.galo.wellcheck.features.counselors.CompleteProfileRequest;
import edu.cit.galo.wellcheck.dto.CompleteCounselorProfileRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    @Value("${supabase.bucket.profile-photos}")
    private String profilePhotosBucket;

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CounselorProfileRepository counselorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StudentProfileFactory studentProfileFactory;
    private final CounselorProfileFactory counselorProfileFactory;

    public AuthService(UserRepository userRepository,
                       StudentProfileRepository studentProfileRepository,
                       CounselorProfileRepository counselorProfileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       StudentProfileFactory studentProfileFactory,
                       CounselorProfileFactory counselorProfileFactory) {
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.counselorProfileRepository = counselorProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.studentProfileFactory = studentProfileFactory;
        this.counselorProfileFactory = counselorProfileFactory;
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

        studentProfileFactory.createAndSaveProfile(user, request);

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

        counselorProfileFactory.createAndSaveProfile(user, request);

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

        String profilePhoto = null;
        String specialization = null;
        if (user.getRole() == UserRole.COUNSELOR) {
            Optional<CounselorProfile> cp = counselorProfileRepository.findByUserId(user.getId());
            profilePhoto = cp.map(CounselorProfile::getProfilePhoto).orElse(null);
            specialization = cp.map(CounselorProfile::getSpecialization).orElse(null);
        }

        return new AuthResponse(
                token,
                user.getRole().name(),
                user.getStatus().name(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                profilePhoto,
                specialization
        );
    }

    public String getEmailFromToken(String token) {
        return jwtUtil.extractEmail(token);
    }

    public AuthResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        String profilePhoto = null;
        String specialization = null;
        if (user.getRole() == UserRole.COUNSELOR) {
            Optional<CounselorProfile> cp = counselorProfileRepository.findByUserId(user.getId());
            profilePhoto = cp.map(CounselorProfile::getProfilePhoto).orElse(null);
            specialization = cp.map(CounselorProfile::getSpecialization).orElse(null);
        }

        return new AuthResponse(
                null,
                user.getRole().name(),
                user.getStatus().name(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                profilePhoto,
                specialization
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

    public StudentProfile getStudentProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        return studentProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found."));
    }

    public CounselorProfile getCounselorProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));
        return counselorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Counselor profile not found."));
    }

    @Transactional
    public String updateCounselorProfile(String email, CompleteCounselorProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        CounselorProfile profile = counselorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Counselor profile not found."));

        if (request.getSpecialization() != null) profile.setSpecialization(request.getSpecialization());
        if (request.getBio() != null)            profile.setBio(request.getBio());
        if (request.getYearsExperience() != null) profile.setYearsExperience(request.getYearsExperience());
        if (request.getLicenseNumber() != null)  profile.setLicenseNumber(request.getLicenseNumber());

        if (request.getCredentials() != null) {
            List<String> entries = request.getCredentials().stream()
                    .map(CredentialItem::toEntry)
                    .collect(Collectors.toList());
            profile.setCredentialEntries(entries);
        }

        if (request.getAvailableDays() != null) profile.setAvailableDays(request.getAvailableDays());

        counselorProfileRepository.save(profile);
        return "Profile updated successfully.";
    }

    @Transactional
    public String uploadCounselorPhoto(String email, MultipartFile file) {
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found."));
            CounselorProfile profile = counselorProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Counselor profile not found."));

            String ext = getExtension(file.getOriginalFilename());
            String fileName = "counselor-" + user.getId() + "-" + System.currentTimeMillis() + ext;

            String uploadUrl = supabaseUrl + "/storage/v1/object/" + profilePhotosBucket + "/" + fileName;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseServiceRoleKey);
            headers.setContentType(MediaType.parseMediaType(
                    file.getContentType() != null ? file.getContentType() : "image/jpeg"));
            headers.set("x-upsert", "true");

            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, String.class);

            String publicUrl = supabaseUrl + "/storage/v1/object/public/" + profilePhotosBucket + "/" + fileName;
            profile.setProfilePhoto(publicUrl);
            profile.setProfilePhotoType("url");
            counselorProfileRepository.save(profile);

            return publicUrl;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to read photo file.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }
}