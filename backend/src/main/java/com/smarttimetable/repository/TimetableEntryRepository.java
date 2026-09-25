package com.smarttimetable.repository;

import com.smarttimetable.entity.TimetableEntry;
import com.smarttimetable.enums.DayOfWeekEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {
    List<TimetableEntry> findByTimetableId(Long timetableId);
    List<TimetableEntry> findByFacultyId(Long facultyId);
    List<TimetableEntry> findByClassroomId(Long classroomId);
    List<TimetableEntry> findByBatchId(Long batchId);
    
    List<TimetableEntry> findByFacultyIdAndDayOfWeekAndPeriodNumber(Long facultyId, DayOfWeekEnum dayOfWeek, Integer periodNumber);
    List<TimetableEntry> findByClassroomIdAndDayOfWeekAndPeriodNumber(Long classroomId, DayOfWeekEnum dayOfWeek, Integer periodNumber);
    List<TimetableEntry> findByBatchIdAndDayOfWeekAndPeriodNumber(Long batchId, DayOfWeekEnum dayOfWeek, Integer periodNumber);
}
