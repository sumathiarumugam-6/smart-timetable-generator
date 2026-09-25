package com.smarttimetable.repository;

import com.smarttimetable.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findByDepartmentId(Long departmentId);
    List<Timetable> findByDepartmentIdAndAcademicYearAndSemester(Long departmentId, String academicYear, Integer semester);
}
