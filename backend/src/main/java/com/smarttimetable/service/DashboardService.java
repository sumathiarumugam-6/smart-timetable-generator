package com.smarttimetable.service;

import com.smarttimetable.dto.DashboardStatsDTO;
import com.smarttimetable.entity.Classroom;
import com.smarttimetable.entity.Faculty;
import com.smarttimetable.entity.TimetableEntry;
import com.smarttimetable.enums.RoomType;

import com.smarttimetable.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final ClassroomRepository classroomRepository;
    private final TimetableRepository timetableRepository;
    private final TimetableEntryRepository timetableEntryRepository;

    public DashboardStatsDTO getDashboardStats() {
        long totalDepts = departmentRepository.count();
        long totalFaculties = facultyRepository.count();
        long totalSubjects = subjectRepository.count();
        long totalClassrooms = classroomRepository.findByRoomType(RoomType.CLASSROOM).size();
        long totalLabs = classroomRepository.findByRoomType(RoomType.LABORATORY).size();
        long generatedTTs = timetableRepository.count();

        // Faculty workload stats
        List<Faculty> faculties = facultyRepository.findAll();
        List<Map<String, Object>> workloadStats = new ArrayList<>();

        for (Faculty f : faculties) {
            List<TimetableEntry> entries = timetableEntryRepository.findByFacultyId(f.getId());
            Map<String, Object> stat = new HashMap<>();
            stat.put("facultyCode", f.getFacultyCode());
            stat.put("name", f.getName());
            stat.put("assignedPeriods", entries.size());
            stat.put("maxHours", f.getMaxWorkingHours());
            workloadStats.add(stat);
        }

        // Classroom utilization stats
        List<Classroom> classrooms = classroomRepository.findAll();
        List<Map<String, Object>> roomStats = new ArrayList<>();

        for (Classroom c : classrooms) {
            List<TimetableEntry> entries = timetableEntryRepository.findByClassroomId(c.getId());
            // Total available slots per week = 6 days * 8 periods = 48
            double utilizationPercent = (entries.size() / 48.0) * 100.0;

            Map<String, Object> stat = new HashMap<>();
            stat.put("roomNumber", c.getRoomNumber());
            stat.put("roomType", c.getRoomType().name());
            stat.put("bookedSlots", entries.size());
            stat.put("utilizationPercentage", Math.round(utilizationPercent * 10.0) / 10.0);
            roomStats.add(stat);
        }

        // Daily slot distribution
        List<Map<String, Object>> dailySlotDist = new ArrayList<>();
        String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
        for (String d : days) {
            Map<String, Object> item = new HashMap<>();
            item.put("day", d);
            item.put("theory", 12);
            item.put("lab", 6);
            dailySlotDist.add(item);
        }

        return DashboardStatsDTO.builder()
                .totalDepartments(totalDepts)
                .totalFaculty(totalFaculties)
                .totalSubjects(totalSubjects)
                .totalClassrooms(totalClassrooms)
                .totalLabs(totalLabs)
                .generatedTimetables(generatedTTs)
                .facultyWorkloadStats(workloadStats)
                .classroomUtilizationStats(roomStats)
                .dailySlotDistribution(dailySlotDist)
                .build();
    }
}
