package edu.cit.galo.wellcheck.service;

import edu.cit.galo.wellcheck.dto.CounselorListItem;
import edu.cit.galo.wellcheck.entity.CounselorProfile;
import edu.cit.galo.wellcheck.enums.SlotStatus;
import edu.cit.galo.wellcheck.enums.UserStatus;
import edu.cit.galo.wellcheck.repository.CounselorProfileRepository;
import edu.cit.galo.wellcheck.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CounselorService {

    private final CounselorProfileRepository counselorProfileRepository;
    private final SlotRepository slotRepository;

    public CounselorService(CounselorProfileRepository counselorProfileRepository,
                            SlotRepository slotRepository) {
        this.counselorProfileRepository = counselorProfileRepository;
        this.slotRepository = slotRepository;
    }

    public List<CounselorListItem> getAllActiveCounselors() {
        return counselorProfileRepository.findByUserStatus(UserStatus.ACTIVE)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }

    public CounselorListItem getCounselorById(Long id) {
        CounselorProfile counselor = counselorProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Counselor not found."));
        return toListItem(counselor);
    }

    private CounselorListItem toListItem(CounselorProfile c) {
        int availableSlots = slotRepository.findByCounselorIdAndStatus(c.getId(), SlotStatus.AVAILABLE).size();
        return new CounselorListItem(
                c.getId(),
                c.getUser().getFirstName(),
                c.getUser().getLastName(),
                c.getUser().getEmail(),
                c.getEmployeeNumber(),
                c.getSpecialization(),
                c.getUser().getStatus().name(),
                c.getBio(),
                availableSlots
        );
    }
}