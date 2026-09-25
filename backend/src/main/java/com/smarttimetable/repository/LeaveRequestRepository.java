package com.smarttimetable.repository;

import com.smarttimetable.entity.LeaveRequest;
import com.smarttimetable.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByFacultyId(Long facultyId);
    List<LeaveRequest> findByStatus(LeaveStatus status);
    List<LeaveRequest> findByFacultyDepartmentId(Long departmentId);
}
