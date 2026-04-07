package edu.cit.galo.wellcheck.factory;

import edu.cit.galo.wellcheck.dto.StudentRegisterRequest;
import edu.cit.galo.wellcheck.entity.StudentProfile;
import edu.cit.galo.wellcheck.entity.User;
import edu.cit.galo.wellcheck.repository.StudentProfileRepository;
import org.springframework.stereotype.Component;

/**
 * Factory for creating Student profiles
 */
@Component
public class StudentProfileFactory implements ProfileFactory {

    private final StudentProfileRepository studentProfileRepository;

    public StudentProfileFactory(StudentProfileRepository studentProfileRepository) {
        this.studentProfileRepository = studentProfileRepository;
    }

    @Override
    public void createAndSaveProfile(User user, Object profileData) {
        StudentRegisterRequest request = (StudentRegisterRequest) profileData;

        StudentProfile profile = new StudentProfile();
        profile.setStudentIdNumber(request.getStudentIdNumber());
        profile.setProgram(request.getProgram());
        profile.setYearLevel(request.getYearLevel());
        profile.setGender(request.getGender());
        profile.setBirthdate(request.getBirthdate()); // Already LocalDate
        profile.setUser(user);

        studentProfileRepository.save(profile);
    }
}