package edu.cit.galo.wellcheck.features.counselors;

import edu.cit.galo.wellcheck.features.auth.CredentialItem;
import edu.cit.galo.wellcheck.features.slots.SlotStatus;
import edu.cit.galo.wellcheck.features.auth.UserStatus;
import edu.cit.galo.wellcheck.features.slots.SlotRepository;
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

    public void rateCounselor(Long counselorProfileId, int rating) {
        CounselorProfile profile = counselorProfileRepository.findById(counselorProfileId)
                .orElseThrow(() -> new RuntimeException("Counselor not found."));

        double currentTotal = profile.getAverageRating() * profile.getRatingCount();
        int newCount = profile.getRatingCount() + 1;
        double newAverage = (currentTotal + rating) / newCount;

        profile.setRatingCount(newCount);
        // Round to 1 decimal place
        profile.setAverageRating(Math.round(newAverage * 10.0) / 10.0);
        counselorProfileRepository.save(profile);
    }

    private CounselorListItem toListItem(CounselorProfile c) {
        int availableSlots = slotRepository
                .findByCounselorIdAndStatus(c.getId(), SlotStatus.AVAILABLE).size();

        List<CredentialItem> credentials = c.getCredentialEntries().stream()
                .map(CredentialItem::fromEntry)
                .collect(Collectors.toList());

        return new CounselorListItem(
                c.getId(),
                c.getUser().getFirstName(),
                c.getUser().getLastName(),
                c.getUser().getEmail(),
                c.getEmployeeNumber(),
                c.getSpecialization(),
                c.getUser().getStatus().name(),
                c.getBio(),
                availableSlots,
                c.getYearsExperience(),
                c.getLicenseNumber(),
                c.getAverageRating(),
                c.getRatingCount(),
                credentials,
                c.getProfilePhoto()
        );
    }
}