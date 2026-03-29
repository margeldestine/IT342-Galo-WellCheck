package edu.cit.galo.wellcheck.repository;

import edu.cit.galo.wellcheck.entity.CounselorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounselorProfileRepository extends JpaRepository<CounselorProfile, Long> {
    boolean existsByEmployeeNumber(String employeeNumber);
}