package edu.cit.galo.wellcheck.controller;

import edu.cit.galo.wellcheck.dto.AppointmentRequest;
import edu.cit.galo.wellcheck.dto.AppointmentResponse;
import edu.cit.galo.wellcheck.security.JwtUtil;
import edu.cit.galo.wellcheck.service.AppointmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final JwtUtil jwtUtil;

    public AppointmentController(AppointmentService appointmentService, JwtUtil jwtUtil) {
        this.appointmentService = appointmentService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<?> bookAppointment(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody AppointmentRequest request) {
        try {
            String email = getEmail(authHeader);
            AppointmentResponse response = appointmentService.bookAppointment(email, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyAppointments(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String email = getEmail(authHeader);
            List<AppointmentResponse> appointments = appointmentService.getMyAppointments(email);
            return ResponseEntity.ok(appointments);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/counselor")
    public ResponseEntity<?> getCounselorAppointments(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String email = getEmail(authHeader);
            List<AppointmentResponse> appointments = appointmentService.getCounselorAppointments(email);
            return ResponseEntity.ok(appointments);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelAppointment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            String email = getEmail(authHeader);
            appointmentService.cancelAppointment(email, id);
            return ResponseEntity.ok("Appointment cancelled successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveAppointment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            String email = getEmail(authHeader);
            AppointmentResponse response = appointmentService.approveAppointment(email, id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectAppointment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        try {
            String email = getEmail(authHeader);
            AppointmentResponse response = appointmentService.rejectAppointment(email, id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private String getEmail(String authHeader) {
        return jwtUtil.extractEmail(authHeader.substring(7));
    }
}