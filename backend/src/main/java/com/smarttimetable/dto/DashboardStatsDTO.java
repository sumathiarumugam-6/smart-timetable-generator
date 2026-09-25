package com.smarttimetable.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private long totalDepartments;
    private long totalFaculty;
    private long totalSubjects;
    private long totalClassrooms;
    private long totalLabs;
    private long generatedTimetables;

    private List<Map<String, Object>> facultyWorkloadStats;
    private List<Map<String, Object>> classroomUtilizationStats;
    private List<Map<String, Object>> dailySlotDistribution;
}
