package edu.cit.galo.wellcheck.features.students;

import edu.cit.galo.wellcheck.domain.interfaces.ProfileFactory;
import edu.cit.galo.wellcheck.domain.entities.StudentProfile;
import edu.cit.galo.wellcheck.domain.entities.User;
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