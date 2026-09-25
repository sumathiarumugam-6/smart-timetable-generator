package com.smarttimetable.repository;

import com.smarttimetable.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    List<Batch> findByDepartmentId(Long departmentId);
    List<Batch> findByDepartmentIdAndSemester(Long departmentId, Integer semester);
    List<Batch> findByAcademicYearAndDepartmentIdAndSemester(String academicYear, Long departmentId, Integer semester);
}
