// UserRepository.java
package com.mgr.campusbridge.repository;
import com.mgr.campusbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRegisterNumber(String registerNumber);
    boolean existsByEmail(String email);
    boolean existsByRegisterNumber(String registerNumber);
    long countByVerified(boolean verified);
    long countByActive(boolean active);

    // Add these methods to your existing UserRepository

    List<User> findByRoleAndAccountStatus(User.Role role, User.AccountStatus status);
    List<User> findByAccountStatus(User.AccountStatus status);
    long countByRole(User.Role role);
    long countByAccountStatus(User.AccountStatus status);
}