package com.mgr.campusbridge.repository;
import com.mgr.campusbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * Approved students for mentor discovery, optionally filtered by a single
     * skill (case-insensitive) and/or a keyword across name/department/bio.
     */
    @Query("""
            SELECT DISTINCT u FROM User u LEFT JOIN u.skills s
            WHERE u.role = com.mgr.campusbridge.entity.User$Role.STUDENT
              AND u.accountStatus = com.mgr.campusbridge.entity.User$AccountStatus.APPROVED
              AND (:skill IS NULL OR LOWER(s) LIKE LOWER(CONCAT('%', :skill, '%')))
              AND (:keyword IS NULL
                   OR LOWER(u.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.department) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(u.bio) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY u.name ASC
            """)
    List<User> searchStudents(@Param("skill") String skill, @Param("keyword") String keyword);

    /**
     * Admin directory search: case-insensitive partial match across name,
     * email and register number.
     */
    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(COALESCE(u.registerNumber, '')) LIKE LOWER(CONCAT('%', :q, '%'))
            ORDER BY u.id ASC
            """)
    List<User> searchDirectory(@Param("q") String q);
}