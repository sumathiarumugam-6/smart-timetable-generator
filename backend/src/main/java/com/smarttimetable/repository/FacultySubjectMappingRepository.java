package com.smarttimetable.repository;

import com.smarttimetable.entity.FacultySubjectMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FacultySubjectMappingRepository extends JpaRepository<FacultySubjectMapping, Long> {
    List<FacultySubjectMapping> findByFacultyId(Long facultyId);
    List<FacultySubjectMapping> findByBatchId(Long batchId);
    List<FacultySubjectMapping> findBySubjectId(Long subjectId);
    List<FacultySubjectMapping> findByBatchDepartmentIdAndBatchSemester(Long departmentId, Integer semester);
}
