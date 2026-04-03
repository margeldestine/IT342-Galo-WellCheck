package edu.cit.galo.wellcheck.repository;

import edu.cit.galo.wellcheck.entity.CounselorProfile;
import edu.cit.galo.wellcheck.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CounselorProfileRepository extends JpaRepository<CounselorProfile, Long> {
    boolean existsByEmployeeNumber(String employeeNumber);
    Optional<CounselorProfile> findByUserId(Long userId);

    @Query("SELECT c FROM CounselorProfile c WHERE c.user.status = :status")
    List<CounselorProfile> findByUserStatus(UserStatus status);
}