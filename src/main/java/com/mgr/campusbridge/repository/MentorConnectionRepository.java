// MentorConnectionRepository.java
package com.mgr.campusbridge.repository;
import com.mgr.campusbridge.entity.MentorConnection;
import com.mgr.campusbridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MentorConnectionRepository extends JpaRepository<MentorConnection, Long> {
    List<MentorConnection> findByStudentAndStatus(User student, MentorConnection.Status status);
    long countByStudentAndStatus(User student, MentorConnection.Status status);
}