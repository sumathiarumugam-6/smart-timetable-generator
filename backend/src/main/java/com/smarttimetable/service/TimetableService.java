package com.smarttimetable.service;

import com.smarttimetable.dto.*;
import com.smarttimetable.entity.*;
import com.smarttimetable.enums.TimetableStatus;
import com.smarttimetable.repository.*;
import com.smarttimetable.service.scheduler.ConflictResolver;
import com.smarttimetable.service.scheduler.Gene;
import com.smarttimetable.service.scheduler.SmartSchedulingEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimetableService {

    private final SmartSchedulingEngine schedulingEngine;
    private final TimetableRepository timetableRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final DepartmentRepository departmentRepository;
    private final ClassroomRepository classroomRepository;
    private final ConflictResolver conflictResolver;
    private final NotificationService notificationService;

    public TimetableDTO generateTimetable(TimetableGenerationRequest req, User creator) {
        Department dept = departmentRepository.findById(req.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found"));

        Timetable generated = schedulingEngine.generateTimetable(
                req.getTitle(), req.getAcademicYear(), req.getSemester(), dept, creator
        );

        // Notify user/admins
        if (creator != null) {
            notificationService.createNotification(
                    creator,
                    "Timetable Generated Successfully",
                    "Timetable '" + req.getTitle() + "' generated with fitness score: " + String.format("%.1f", generated.getFitnessScore()),
                    "SUCCESS"
            );
        }

        return mapToDTO(generated);
    }

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void seedInitialDepartmentTimetables() {
        try {
            List<Department> departments = departmentRepository.findAll();
            for (Department dept : departments) {
                if (timetableRepository.findByDepartmentId(dept.getId()).isEmpty()) {
                    TimetableGenerationRequest req = new TimetableGenerationRequest();
                    req.setDepartmentId(dept.getId());
                    req.setSemester(5);
                    req.setAcademicYear("2025-2026");
                    req.setTitle(dept.getName() + " - Semester 5 Master Timetable");
                    generateTimetable(req, null);
                }
            }
        } catch (Exception e) {
            System.err.println("Notice: Initial department timetable generation: " + e.getMessage());
        }
    }

    public List<TimetableDTO> getAllTimetables() {
        return timetableRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    public TimetableDTO getTimetableById(Long id) {
        Timetable t = timetableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timetable not found with ID: " + id));
        return mapToDTO(t);
    }

    public TimetableDTO performManualSwap(ManualSwapRequest swapReq) {
        TimetableEntry entry = timetableEntryRepository.findById(swapReq.getEntryId1())
                .orElseThrow(() -> new RuntimeException("Timetable entry not found"));

        Timetable timetable = entry.getTimetable();

        // Convert entries to genes to validate conflict
        List<TimetableEntry> allEntries = timetableEntryRepository.findByTimetableId(timetable.getId());
        List<Gene> genes = new ArrayList<>();

        Gene targetGene = null;
        for (TimetableEntry e : allEntries) {
            Gene g = Gene.builder()
                    .dayOfWeek(e.getDayOfWeek())
                    .periodNumber(e.getPeriodNumber())
                    .timeSlot(e.getTimeSlot())
                    .batchId(e.getBatch().getId())
                    .subjectId(e.getSubject().getId())
                    .facultyId(e.getFaculty().getId())
                    .classroomId(e.getClassroom().getId())
                    .isLabSession(e.getIsLabSession())
                    .build();
            genes.add(g);

            if (e.getId().equals(entry.getId())) {
                targetGene = g;
            }
        }

        Classroom newRoom = swapReq.getTargetClassroomId() != null ?
                classroomRepository.findById(swapReq.getTargetClassroomId()).orElse(entry.getClassroom()) : entry.getClassroom();

        boolean isValid = conflictResolver.validateManualSwap(
                genes, targetGene, swapReq.getTargetDay(), swapReq.getTargetPeriodNumber(), newRoom.getId()
        );

        if (!isValid) {
            throw new RuntimeException("Manual swap failed: Conflict detected in target slot!");
        }

        entry.setDayOfWeek(swapReq.getTargetDay());
        entry.setPeriodNumber(swapReq.getTargetPeriodNumber());
        entry.setClassroom(newRoom);
        timetableEntryRepository.save(entry);

        return mapToDTO(timetableRepository.findById(timetable.getId()).get());
    }

    public TimetableDTO updateStatus(Long id, TimetableStatus status) {
        Timetable t = timetableRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Timetable not found"));

        t.setStatus(status);
        return mapToDTO(timetableRepository.save(t));
    }

    public void deleteTimetable(Long id) {
        timetableRepository.deleteById(id);
    }

    public TimetableDTO mapToDTO(Timetable t) {
        List<TimetableEntry> entries = timetableEntryRepository.findByTimetableId(t.getId());

        List<TimetableEntryDTO> entryDTOs = entries.stream().map(e -> TimetableEntryDTO.builder()
                .id(e.getId())
                .timetableId(t.getId())
                .dayOfWeek(e.getDayOfWeek())
                .periodNumber(e.getPeriodNumber())
                .timeSlot(e.getTimeSlot())
                .batchId(e.getBatch().getId())
                .batchSection(e.getBatch().getSection())
                .semester(e.getBatch().getSemester())
                .subjectId(e.getSubject().getId())
                .subjectCode(e.getSubject().getSubjectCode())
                .subjectName(e.getSubject().getSubjectName())
                .subjectType(e.getSubject().getSubjectType())
                .facultyId(e.getFaculty().getId())
                .facultyCode(e.getFaculty().getFacultyCode())
                .facultyName(e.getFaculty().getName())
                .classroomId(e.getClassroom().getId())
                .roomNumber(e.getClassroom().getRoomNumber())
                .building(e.getClassroom().getBuilding())
                .isLabSession(e.getIsLabSession())
                .labGroup(e.getLabGroup())
                .build()).toList();

        return TimetableDTO.builder()
                .id(t.getId())
                .title(t.getTitle())
                .academicYear(t.getAcademicYear())
                .semester(t.getSemester())
                .departmentId(t.getDepartment().getId())
                .departmentName(t.getDepartment().getName())
                .status(t.getStatus())
                .fitnessScore(t.getFitnessScore())
                .conflictCount(t.getConflictCount())
                .createdByName(t.getCreatedBy() != null ? t.getCreatedBy().getFullName() : "System")
                .createdAt(t.getCreatedAt())
                .entries(entryDTOs)
                .build();
    }
}
