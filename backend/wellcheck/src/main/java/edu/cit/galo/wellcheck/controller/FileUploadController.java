package edu.cit.galo.wellcheck.controller;

import edu.cit.galo.wellcheck.repository.StudentProfileRepository;
import edu.cit.galo.wellcheck.repository.UserRepository;
import edu.cit.galo.wellcheck.security.JwtUtil;
import edu.cit.galo.wellcheck.service.FileUploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final JwtUtil jwtUtil;

    public FileUploadController(FileUploadService fileUploadService,
                                UserRepository userRepository,
                                StudentProfileRepository studentProfileRepository,
                                JwtUtil jwtUtil) {
        this.fileUploadService = fileUploadService;
        this.userRepository = userRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/school-id")
    public ResponseEntity<?> uploadSchoolId(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file) {
        try {
            String email = jwtUtil.extractEmail(authHeader.substring(7));

            var user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found."));

            var studentProfile = studentProfileRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new RuntimeException("Student profile not found."));

            String extension = "";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            String fileName = "school-id-" + user.getId() + "-" + UUID.randomUUID() + extension;
            String url = fileUploadService.uploadSchoolId(file, fileName);

            studentProfile.setSchoolIdPhotoUrl(url);
            studentProfileRepository.save(studentProfile);

            return ResponseEntity.ok(url);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}