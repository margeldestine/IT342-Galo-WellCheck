package edu.cit.galo.wellcheck.repository;

import edu.cit.galo.wellcheck.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    boolean existsByStudentIdNumber(String studentIdNumber);
    Optional<StudentProfile> findByUserId(Long userId);
}