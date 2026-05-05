package edu.cit.galo.wellcheck.features.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    long countByRole(UserRole role);
    long countByRoleAndStatus(UserRole role, UserStatus status);
    List<User> findByRole(UserRole role);
}