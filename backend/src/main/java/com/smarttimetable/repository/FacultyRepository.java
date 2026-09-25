package com.smarttimetable.repository;

import com.smarttimetable.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByFacultyCode(String facultyCode);
    Optional<Faculty> findByUserId(Long userId);
    List<Faculty> findByDepartmentId(Long departmentId);
    List<Faculty> findByDepartmentIdAndIsActiveTrue(Long departmentId);
}
