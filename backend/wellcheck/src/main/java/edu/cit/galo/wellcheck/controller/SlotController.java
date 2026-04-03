package edu.cit.galo.wellcheck.controller;

import edu.cit.galo.wellcheck.dto.SlotRequest;
import edu.cit.galo.wellcheck.dto.SlotResponse;
import edu.cit.galo.wellcheck.security.JwtUtil;
import edu.cit.galo.wellcheck.service.SlotService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slots")
@CrossOrigin(origins = "*")
public class SlotController {

    private final SlotService slotService;
    private final JwtUtil jwtUtil;

    public SlotController(SlotService slotService, JwtUtil jwtUtil) {
        this.slotService = slotService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<?> createSlot(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody SlotRequest request) {
        try {
            String email = getEmail(authHeader);
            SlotResponse response = slotService.createSlot(email, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMySlots(
            @RequestHeader("Authorization") String authHeader) {
        try {
            String email = getEmail(authHeader);
            List<SlotResponse> slots = slotService.getMySLots(email);
            return ResponseEntity.ok(slots);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/counselor/{counselorId}")
    public ResponseEntity<?> getCounselorAvailableSlots(
            @PathVariable Long counselorId) {
        try {
            List<SlotResponse> slots = slotService.getCounselorAvailableSlots(counselorId);
            return ResponseEntity.ok(slots);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{slotId}")
    public ResponseEntity<?> updateSlot(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long slotId,
            @RequestBody SlotRequest request) {
        try {
            String email = getEmail(authHeader);
            SlotResponse response = slotService.updateSlot(email, slotId, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<?> deleteSlot(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long slotId) {
        try {
            String email = getEmail(authHeader);
            slotService.deleteSlot(email, slotId);
            return ResponseEntity.ok("Slot deleted successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private String getEmail(String authHeader) {
        return jwtUtil.extractEmail(authHeader.substring(7));
    }
}