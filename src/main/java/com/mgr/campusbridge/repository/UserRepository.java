// UserRepository.java
package com.mgr.campusbridge.repository;
import com.mgr.campusbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByVerified(boolean verified);
    long countByActive(boolean active);
}