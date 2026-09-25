package com.smarttimetable.controller;

import com.smarttimetable.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/faculty/{facultyId}/export")
    public ResponseEntity<byte[]> exportFacultyReport(@PathVariable Long facultyId) {
        byte[] csvData = reportService.generateFacultyReportCsv(facultyId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=faculty_timetable_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    @GetMapping("/classrooms/export")
    public ResponseEntity<byte[]> exportClassroomReport() {
        byte[] csvData = reportService.generateClassroomUtilizationCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=classroom_utilization_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    @GetMapping("/workload/export")
    public ResponseEntity<byte[]> exportWorkloadReport() {
        byte[] csvData = reportService.generateFacultyWorkloadCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=faculty_workload_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }
}
