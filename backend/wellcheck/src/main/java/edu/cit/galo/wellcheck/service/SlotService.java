package edu.cit.galo.wellcheck.service;

import edu.cit.galo.wellcheck.dto.SlotRequest;
import edu.cit.galo.wellcheck.dto.SlotResponse;
import edu.cit.galo.wellcheck.entity.CounselorProfile;
import edu.cit.galo.wellcheck.entity.Slot;
import edu.cit.galo.wellcheck.enums.SlotStatus;
import edu.cit.galo.wellcheck.repository.CounselorProfileRepository;
import edu.cit.galo.wellcheck.repository.SlotRepository;
import edu.cit.galo.wellcheck.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SlotService {

    private final SlotRepository slotRepository;
    private final CounselorProfileRepository counselorProfileRepository;
    private final UserRepository userRepository;

    public SlotService(SlotRepository slotRepository,
                       CounselorProfileRepository counselorProfileRepository,
                       UserRepository userRepository) {
        this.slotRepository = slotRepository;
        this.counselorProfileRepository = counselorProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SlotResponse createSlot(String email, SlotRequest request) {
        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Slot start time must be in the future.");
        }
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new RuntimeException("End time must be after start time.");
        }

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        CounselorProfile counselor = counselorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Counselor profile not found."));

        Slot slot = new Slot();
        slot.setCounselor(counselor);
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setStatus(SlotStatus.AVAILABLE);

        Slot saved = slotRepository.save(slot);
        return toResponse(saved);
    }

    public List<SlotResponse> getMySLots(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        CounselorProfile counselor = counselorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Counselor profile not found."));

        return slotRepository.findByCounselorId(counselor.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<SlotResponse> getCounselorAvailableSlots(Long counselorId) {
        return slotRepository.findByCounselorIdAndStatus(counselorId, SlotStatus.AVAILABLE)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SlotResponse updateSlot(String email, Long slotId, SlotRequest request) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found."));

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        CounselorProfile counselor = counselorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Counselor profile not found."));

        if (!slot.getCounselor().getId().equals(counselor.getId())) {
            throw new RuntimeException("You are not authorized to update this slot.");
        }

        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new RuntimeException("Cannot update a booked slot.");
        }

        if (request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Slot start time must be in the future.");
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new RuntimeException("End time must be after start time.");
        }

        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());

        Slot updated = slotRepository.save(slot);
        return toResponse(updated);
    }

    @Transactional
    public void deleteSlot(String email, Long slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found."));

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));

        CounselorProfile counselor = counselorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Counselor profile not found."));

        if (!slot.getCounselor().getId().equals(counselor.getId())) {
            throw new RuntimeException("You are not authorized to delete this slot.");
        }

        if (slot.getStatus() == SlotStatus.BOOKED) {
            throw new RuntimeException("Cannot delete a booked slot.");
        }

        slotRepository.delete(slot);
    }

    private SlotResponse toResponse(Slot slot) {
        return new SlotResponse(
                slot.getId(),
                slot.getCounselor().getId(),
                slot.getCounselor().getUser().getFirstName(),
                slot.getCounselor().getUser().getLastName(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus().name(),
                slot.getCreatedAt()
        );
    }
}