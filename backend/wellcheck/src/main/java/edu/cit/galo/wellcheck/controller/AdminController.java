package edu.cit.galo.wellcheck.controller;

import edu.cit.galo.wellcheck.dto.AdminDashboardStats;
import edu.cit.galo.wellcheck.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/counselors")
    public ResponseEntity<?> getAllCounselors() {
        return ResponseEntity.ok(adminService.getAllCounselors());
    }

    @PutMapping("/counselors/{id}/approve")
    public ResponseEntity<?> approveCounselor(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminService.approveCounselor(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/counselors/{id}/reject")
    public ResponseEntity<?> rejectCounselor(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminService.rejectCounselor(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/counselors/{id}/deactivate")
    public ResponseEntity<?> deactivateCounselor(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(adminService.deactivateCounselor(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/students")
    public ResponseEntity<?> getAllStudents() {
        return ResponseEntity.ok(adminService.getAllStudents());
    }
}