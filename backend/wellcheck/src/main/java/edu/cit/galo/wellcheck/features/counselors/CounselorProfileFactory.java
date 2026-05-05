package edu.cit.galo.wellcheck.features.counselors;

import edu.cit.galo.wellcheck.features.auth.ProfileFactory;
import edu.cit.galo.wellcheck.features.auth.User;
import org.springframework.stereotype.Component;

/**
 * Factory for creating Counselor profiles
 */
@Component
public class CounselorProfileFactory implements ProfileFactory {

    private final CounselorProfileRepository counselorProfileRepository;

    public CounselorProfileFactory(CounselorProfileRepository counselorProfileRepository) {
        this.counselorProfileRepository = counselorProfileRepository;
    }

    @Override
    public void createAndSaveProfile(User user, Object profileData) {
        CounselorRegisterRequest request = (CounselorRegisterRequest) profileData;

        CounselorProfile profile = new CounselorProfile();
        profile.setEmployeeNumber(request.getEmployeeNumber());
        profile.setSpecialization(request.getSpecialization());
        profile.setBio(request.getBio());
        profile.setUser(user);

        counselorProfileRepository.save(profile);
    }
}