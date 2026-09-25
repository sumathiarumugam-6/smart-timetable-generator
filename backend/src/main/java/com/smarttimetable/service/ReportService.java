package com.smarttimetable.service;

import com.smarttimetable.entity.*;
import com.smarttimetable.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TimetableRepository timetableRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final FacultyRepository facultyRepository;
    private final ClassroomRepository classroomRepository;

    public byte[] generateFacultyReportCsv(Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));

        List<TimetableEntry> entries = timetableEntryRepository.findByFacultyId(facultyId);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("FACULTY TIMETABLE REPORT");
        writer.println("Faculty Name: " + faculty.getName() + " (" + faculty.getFacultyCode() + ")");
        writer.println("Department: " + faculty.getDepartment().getName());
        writer.println();
        writer.println("Day,Period,Time Slot,Subject Code,Subject Name,Batch Section,Classroom");

        for (TimetableEntry e : entries) {
            writer.printf("%s,%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    e.getDayOfWeek(),
                    e.getPeriodNumber(),
                    e.getTimeSlot(),
                    e.getSubject().getSubjectCode(),
                    e.getSubject().getSubjectName(),
                    e.getBatch().getSection(),
                    e.getClassroom().getRoomNumber());
        }

        writer.flush();
        return out.toByteArray();
    }

    public byte[] generateClassroomUtilizationCsv() {
        List<Classroom> rooms = classroomRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("CLASSROOM & LAB UTILIZATION REPORT");
        writer.println("Room Number,Building,Type,Capacity,Booked Slots (Weekly),Utilization %");

        for (Classroom c : rooms) {
            List<TimetableEntry> entries = timetableEntryRepository.findByClassroomId(c.getId());
            double util = (entries.size() / 48.0) * 100.0;
            writer.printf("\"%s\",\"%s\",\"%s\",%d,%d,%.1f%%\n",
                    c.getRoomNumber(),
                    c.getBuilding(),
                    c.getRoomType(),
                    c.getCapacity(),
                    entries.size(),
                    util);
        }

        writer.flush();
        return out.toByteArray();
    }

    public byte[] generateFacultyWorkloadCsv() {
        List<Faculty> faculties = facultyRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out);

        writer.println("FACULTY WORKLOAD REPORT");
        writer.println("Faculty Code,Faculty Name,Designation,Department,Max Limit (Hours),Assigned Periods,Status");

        for (Faculty f : faculties) {
            List<TimetableEntry> entries = timetableEntryRepository.findByFacultyId(f.getId());
            String status = entries.size() > f.getMaxWorkingHours() ? "OVERLOAD" : "NORMAL";

            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",%d,%d,%s\n",
                    f.getFacultyCode(),
                    f.getName(),
                    f.getDesignation(),
                    f.getDepartment().getName(),
                    f.getMaxWorkingHours(),
                    entries.size(),
                    status);
        }

        writer.flush();
        return out.toByteArray();
    }
}
