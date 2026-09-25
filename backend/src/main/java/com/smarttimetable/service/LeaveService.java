package com.smarttimetable.service;

import com.smarttimetable.dto.LeaveRequestDTO;
import com.smarttimetable.entity.*;
import com.smarttimetable.enums.LeaveStatus;
import com.smarttimetable.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRepository;
    private final FacultyRepository facultyRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final NotificationService notificationService;

    public List<LeaveRequestDTO> getAllLeaveRequests() {
        return leaveRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public List<LeaveRequestDTO> getLeaveRequestsByFaculty(Long facultyId) {
        return leaveRepository.findByFacultyId(facultyId).stream().map(this::mapToDTO).toList();
    }

    public LeaveRequestDTO applyLeave(LeaveRequestDTO dto) {
        Faculty faculty = facultyRepository.findById(dto.getFacultyId())
                .orElseThrow(() -> new RuntimeException("Faculty not found"));

        LeaveRequest leave = LeaveRequest.builder()
                .faculty(faculty)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        LeaveRequest saved = leaveRepository.save(leave);

        if (faculty.getUser() != null) {
            notificationService.createNotification(
                    faculty.getUser(),
                    "Leave Request Submitted",
                    "Your leave request from " + dto.getStartDate() + " to " + dto.getEndDate() + " has been submitted.",
                    "INFO"
            );
        }

        return mapToDTO(saved);
    }

    public LeaveRequestDTO approveLeave(Long leaveId, User approver) {
        LeaveRequest leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        Faculty mainFaculty = leave.getFaculty();

        // Find available substitute faculty in the same department
        List<Faculty> availableSubstitutes = facultyRepository.findByDepartmentIdAndIsActiveTrue(mainFaculty.getDepartment().getId())
                .stream()
                .filter(f -> !f.getId().equals(mainFaculty.getId()))
                .toList();

        Faculty substitute = availableSubstitutes.isEmpty() ? null : availableSubstitutes.get(0);

        leave.setStatus(LeaveStatus.APPROVED);
        leave.setSubstituteFaculty(substitute);
        leave.setApprovedBy(approver);

        LeaveRequest updated = leaveRepository.save(leave);

        // Update active timetable entries automatically if substitute assigned
        if (substitute != null) {
            List<TimetableEntry> entries = timetableEntryRepository.findByFacultyId(mainFaculty.getId());
            for (TimetableEntry e : entries) {
                e.setFaculty(substitute);
            }
            timetableEntryRepository.saveAll(entries);

            // Notify substitute faculty
            if (substitute.getUser() != null) {
                notificationService.createNotification(
                        substitute.getUser(),
                        "Substitute Faculty Assignment",
                        "You have been assigned as substitute faculty for " + mainFaculty.getName() + " due to approved leave.",
                        "WARNING"
                );
            }
        }

        // Notify applying faculty
        if (mainFaculty.getUser() != null) {
            notificationService.createNotification(
                    mainFaculty.getUser(),
                    "Leave Approved",
                    "Your leave request has been approved. Substitute assigned: " + (substitute != null ? substitute.getName() : "None"),
                    "SUCCESS"
            );
        }

        return mapToDTO(updated);
    }

    public LeaveRequestDTO rejectLeave(Long leaveId, User rejecter) {
        LeaveRequest leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new RuntimeException("Leave request not found"));

        leave.setStatus(LeaveStatus.REJECTED);
        leave.setApprovedBy(rejecter);

        LeaveRequest updated = leaveRepository.save(leave);

        if (leave.getFaculty().getUser() != null) {
            notificationService.createNotification(
                    leave.getFaculty().getUser(),
                    "Leave Request Rejected",
                    "Your leave request from " + leave.getStartDate() + " to " + leave.getEndDate() + " was rejected.",
                    "DANGER"
            );
        }

        return mapToDTO(updated);
    }

    public LeaveRequestDTO mapToDTO(LeaveRequest l) {
        return LeaveRequestDTO.builder()
                .id(l.getId())
                .facultyId(l.getFaculty().getId())
                .facultyCode(l.getFaculty().getFacultyCode())
                .facultyName(l.getFaculty().getName())
                .startDate(l.getStartDate())
                .endDate(l.getEndDate())
                .reason(l.getReason())
                .status(l.getStatus())
                .substituteFacultyId(l.getSubstituteFaculty() != null ? l.getSubstituteFaculty().getId() : null)
                .substituteFacultyName(l.getSubstituteFaculty() != null ? l.getSubstituteFaculty().getName() : null)
                .approvedByName(l.getApprovedBy() != null ? l.getApprovedBy().getFullName() : null)
                .createdAt(l.getCreatedAt())
                .build();
    }
}
