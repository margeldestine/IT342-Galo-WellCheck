package edu.cit.galo.wellcheck.features.students;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    boolean existsByStudentIdNumber(String studentIdNumber);
    Optional<StudentProfile> findByUserId(Long userId);
}