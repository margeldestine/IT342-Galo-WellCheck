package edu.cit.galo.wellcheck.controller;

import edu.cit.galo.wellcheck.service.CounselorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/counselors")
@CrossOrigin(origins = "*")
public class CounselorController {

    private final CounselorService counselorService;

    public CounselorController(CounselorService counselorService) {
        this.counselorService = counselorService;
    }

    @GetMapping
    public ResponseEntity<?> getAllActiveCounselors() {
        return ResponseEntity.ok(counselorService.getAllActiveCounselors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCounselorById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(counselorService.getCounselorById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}