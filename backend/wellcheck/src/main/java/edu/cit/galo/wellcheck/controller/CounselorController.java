package edu.cit.galo.wellcheck.controller;

import edu.cit.galo.wellcheck.dto.RatingRequest;
import edu.cit.galo.wellcheck.core.security.JwtUtil;
import edu.cit.galo.wellcheck.service.CounselorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/counselors")
@CrossOrigin(origins = "*")
public class CounselorController {

    private final CounselorService counselorService;
    private final JwtUtil jwtUtil;

    public CounselorController(CounselorService counselorService, JwtUtil jwtUtil) {
        this.counselorService = counselorService;
        this.jwtUtil = jwtUtil;
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

    /** POST /counselors/{id}/rate — student submits a 1–5 star rating */
    @PostMapping("/{id}/rate")
    public ResponseEntity<?> rateCounselor(
            @PathVariable Long id,
            @RequestBody RatingRequest req,
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (req.getRating() < 1 || req.getRating() > 5) {
                return ResponseEntity.badRequest().body("Rating must be between 1 and 5.");
            }
            counselorService.rateCounselor(id, req.getRating());
            return ResponseEntity.ok("Rating submitted.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}