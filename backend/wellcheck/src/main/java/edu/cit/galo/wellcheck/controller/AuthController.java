package edu.cit.galo.wellcheck.controller;

import edu.cit.galo.wellcheck.dto.AuthResponse;
import edu.cit.galo.wellcheck.dto.CounselorRegisterRequest;
import edu.cit.galo.wellcheck.dto.LoginRequest;
import edu.cit.galo.wellcheck.dto.StudentRegisterRequest;
import edu.cit.galo.wellcheck.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/student")
    public ResponseEntity<?> registerStudent(@Valid @RequestBody StudentRegisterRequest request) {
        try {
            String response = authService.registerStudent(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/register/counselor")
    public ResponseEntity<?> registerCounselor(@Valid @RequestBody CounselorRegisterRequest request) {
        try {
            String response = authService.registerCounselor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7);
            String email = authService.getEmailFromToken(token);
            return ResponseEntity.ok(authService.getCurrentUser(email));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
        }
    }

}