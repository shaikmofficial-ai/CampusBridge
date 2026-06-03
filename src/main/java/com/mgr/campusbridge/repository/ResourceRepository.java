// ResourceRepository.java
package com.mgr.campusbridge.repository;
import com.mgr.campusbridge.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByType(Resource.ResourceType type);
    List<Resource> findByDepartment(String department);

    // Add this to your existing ResourceRepository

    List<Resource> findAllByOrderByUploadedAtDesc();
    List<Resource> findByTypeOrderByUploadedAtDesc(Resource.ResourceType type);
}