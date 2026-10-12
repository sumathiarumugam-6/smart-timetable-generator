package com.smarttimetable.service.scheduler;

import com.smarttimetable.entity.*;
import com.smarttimetable.enums.DayOfWeekEnum;
import com.smarttimetable.enums.RoomType;
import com.smarttimetable.enums.SubjectType;
import com.smarttimetable.enums.TimetableStatus;
import com.smarttimetable.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SmartSchedulingEngine {

    private final GeneticAlgorithmScheduler gaScheduler;
    private final ConflictResolver conflictResolver;
    private final FacultySubjectMappingRepository mappingRepository;
    private final ClassroomRepository classroomRepository;
    private final TimetableRepository timetableRepository;
    private final TimetableEntryRepository timetableEntryRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final BatchRepository batchRepository;

    public static final String[] TIME_SLOTS = {
        "8:30 AM – 9:20 AM",    // Period 1
        "9:20 AM – 10:10 AM",   // Period 2
        "10:25 AM – 11:15 AM",  // Period 3
        "11:15 AM – 12:05 PM",  // Period 4
        "12:50 PM – 1:40 PM",   // Period 5
        "1:40 PM – 2:30 PM",    // Period 6
        "2:45 PM – 3:35 PM",    // Period 7
        "3:35 PM – 4:25 PM"     // Period 8
    };

    public Timetable generateTimetable(String title, String academicYear, Integer semester, Department department, User creator) {
        List<FacultySubjectMapping> mappings = mappingRepository.findByBatchDepartmentIdAndBatchSemester(department.getId(), semester);

        // Fallback: If no mappings exist for department & semester, auto-generate default curriculum & mappings!
        if (mappings.isEmpty()) {
            mappings = createFallbackCurriculumAndMappings(department, semester, academicYear);
        }

        List<Classroom> classrooms = classroomRepository.findByDepartmentId(department.getId());
        if (classrooms.isEmpty()) {
            classrooms = classroomRepository.findAll();
        }
        if (classrooms.isEmpty()) {
            Classroom c1 = classroomRepository.save(Classroom.builder()
                    .roomNumber(department.getCode() + "-101")
                    .building(department.getBuilding() != null ? department.getBuilding() : "Main Block")
                    .capacity(70)
                    .roomType(RoomType.CLASSROOM)
                    .department(department)
                    .build());
            Classroom c2 = classroomRepository.save(Classroom.builder()
                    .roomNumber(department.getCode() + "-LAB1")
                    .building(department.getBuilding() != null ? department.getBuilding() : "Main Block")
                    .capacity(40)
                    .roomType(RoomType.LABORATORY)
                    .department(department)
                    .build());
            classrooms = new ArrayList<>(List.of(c1, c2));
        }

        List<Classroom> theoryRooms = classrooms.stream().filter(c -> c.getRoomType() == RoomType.CLASSROOM).toList();
        List<Classroom> labRooms = classrooms.stream().filter(c -> c.getRoomType() == RoomType.LABORATORY).toList();

        if (theoryRooms.isEmpty()) theoryRooms = classrooms;
        if (labRooms.isEmpty()) labRooms = classrooms;

        List<Gene> templateGenes = new ArrayList<>();
        Random random = new Random();

        for (FacultySubjectMapping map : mappings) {
            Subject subject = map.getSubject();
            Faculty faculty = map.getFaculty();
            Batch batch = map.getBatch();
            int weeklyPeriods = subject.getWeeklyPeriods() != null ? subject.getWeeklyPeriods() : 3;

            boolean isLab = "LAB".equalsIgnoreCase(subject.getSubjectType().name());

            if (isLab) {
                // Labs allocate 3 continuous periods in afternoon (Periods 5, 6, 7)
                DayOfWeekEnum randomDay = DayOfWeekEnum.values()[random.nextInt(6)];
                Classroom selectedLabRoom = !labRooms.isEmpty() ? labRooms.get(random.nextInt(labRooms.size())) : classrooms.get(0);

                for (int period = 5; period <= 7; period++) {
                    Gene g = Gene.builder()
                            .dayOfWeek(randomDay)
                            .periodNumber(period)
                            .timeSlot(TIME_SLOTS[period - 1])
                            .batchId(batch.getId())
                            .batchSection(batch.getSection())
                            .subjectId(subject.getId())
                            .subjectCode(subject.getSubjectCode())
                            .subjectType(subject.getSubjectType().name())
                            .facultyId(faculty.getId())
                            .facultyName(faculty.getName())
                            .classroomId(selectedLabRoom.getId())
                            .roomNumber(selectedLabRoom.getRoomNumber())
                            .roomType(selectedLabRoom.getRoomType().name())
                            .isLabSession(true)
                            .build();
                    templateGenes.add(g);
                }
            } else {
                for (int p = 0; p < weeklyPeriods; p++) {
                    DayOfWeekEnum day = DayOfWeekEnum.values()[(p * 2) % 6];
                    int periodNum = (p % 4) + 1;
                    Classroom selectedRoom = map.getPreferredClassroom() != null ? map.getPreferredClassroom() : theoryRooms.get(p % theoryRooms.size());

                    Gene g = Gene.builder()
                            .dayOfWeek(day)
                            .periodNumber(periodNum)
                            .timeSlot(TIME_SLOTS[periodNum - 1])
                            .batchId(batch.getId())
                            .batchSection(batch.getSection())
                            .subjectId(subject.getId())
                            .subjectCode(subject.getSubjectCode())
                            .subjectType(subject.getSubjectType().name())
                            .facultyId(faculty.getId())
                            .facultyName(faculty.getName())
                            .classroomId(selectedRoom.getId())
                            .roomNumber(selectedRoom.getRoomNumber())
                            .roomType(selectedRoom.getRoomType().name())
                            .isLabSession(false)
                            .build();
                    templateGenes.add(g);
                }
            }
        }

        Chromosome bestChromosome = gaScheduler.runOptimization(templateGenes, 100, 300);
        conflictResolver.resolveRemainingConflicts(bestChromosome);

        Timetable timetable = Timetable.builder()
                .title(title)
                .academicYear(academicYear)
                .semester(semester)
                .department(department)
                .status(TimetableStatus.GENERATED)
                .fitnessScore(bestChromosome.getFitnessScore())
                .conflictCount(bestChromosome.getHardConflictCount())
                .createdBy(creator)
                .build();

        timetable = timetableRepository.save(timetable);

        Map<Long, Batch> batchMap = batchRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(Batch::getId, b -> b, (b1, b2) -> b1));
        Map<Long, Subject> subjectMap = subjectRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(Subject::getId, s -> s, (s1, s2) -> s1));
        Map<Long, Faculty> facultyMap = facultyRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(Faculty::getId, f -> f, (f1, f2) -> f1));
        Map<Long, Classroom> classroomMap = classroomRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(Classroom::getId, c -> c, (c1, c2) -> c1));

        List<TimetableEntry> entries = new ArrayList<>();
        for (Gene g : bestChromosome.getGenes()) {
            TimetableEntry entry = TimetableEntry.builder()
                    .timetable(timetable)
                    .dayOfWeek(g.getDayOfWeek())
                    .periodNumber(g.getPeriodNumber())
                    .timeSlot(TIME_SLOTS[g.getPeriodNumber() - 1])
                    .batch(batchMap.getOrDefault(g.getBatchId(), Batch.builder().id(g.getBatchId()).build()))
                    .subject(subjectMap.getOrDefault(g.getSubjectId(), Subject.builder().id(g.getSubjectId()).build()))
                    .faculty(facultyMap.getOrDefault(g.getFacultyId(), Faculty.builder().id(g.getFacultyId()).build()))
                    .classroom(classroomMap.getOrDefault(g.getClassroomId(), Classroom.builder().id(g.getClassroomId()).build()))
                    .isLabSession(g.getIsLabSession())
                    .build();
            entries.add(entry);
        }

        timetableEntryRepository.saveAll(entries);
        timetable.getEntries().clear();
        timetable.getEntries().addAll(entries);

        return timetable;
    }

    private List<FacultySubjectMapping> createFallbackCurriculumAndMappings(Department dept, Integer semester, String academicYear) {
        // 1. Ensure Batch exists
        List<Batch> deptBatches = batchRepository.findByDepartmentIdAndSemester(dept.getId(), semester);
        Batch batch;
        if (deptBatches.isEmpty()) {
            batch = batchRepository.save(Batch.builder()
                    .academicYear(academicYear)
                    .department(dept)
                    .semester(semester)
                    .section("A")
                    .studentCount(60)
                    .build());
        } else {
            batch = deptBatches.get(0);
        }

        // 2. Ensure Faculty members exist
        List<Faculty> deptFaculties = facultyRepository.findByDepartmentId(dept.getId());
        if (deptFaculties.isEmpty()) {
            Faculty f1 = facultyRepository.save(Faculty.builder()
                    .facultyCode("FAC_" + dept.getCode() + "_1")
                    .name("Dr. " + dept.getHodName())
                    .designation("Professor & HOD")
                    .department(dept)
                    .email("hod." + dept.getCode().toLowerCase() + "@enggcollege.edu")
                    .maxWorkingHours(20)
                    .build());
            Faculty f2 = facultyRepository.save(Faculty.builder()
                    .facultyCode("FAC_" + dept.getCode() + "_2")
                    .name("Prof. " + dept.getCode() + " Senior Lead")
                    .designation("Associate Professor")
                    .department(dept)
                    .email("prof." + dept.getCode().toLowerCase() + "@enggcollege.edu")
                    .maxWorkingHours(20)
                    .build());
            deptFaculties = List.of(f1, f2);
        }

        // 3. Ensure Subjects exist for semester
        List<Subject> deptSubjects = subjectRepository.findByDepartmentIdAndSemester(dept.getId(), semester);
        if (deptSubjects.isEmpty()) {
            Subject s1 = subjectRepository.save(Subject.builder()
                    .subjectCode(dept.getCode() + (semester * 100 + 1))
                    .subjectName("Advanced " + dept.getName() + " Theory")
                    .credits(4)
                    .subjectType(SubjectType.CORE)
                    .semester(semester)
                    .weeklyPeriods(4)
                    .department(dept)
                    .build());

            Subject s2 = subjectRepository.save(Subject.builder()
                    .subjectCode(dept.getCode() + (semester * 100 + 2))
                    .subjectName(dept.getName() + " Systems Engineering")
                    .credits(4)
                    .subjectType(SubjectType.CORE)
                    .semester(semester)
                    .weeklyPeriods(4)
                    .department(dept)
                    .build());

            Subject s3 = subjectRepository.save(Subject.builder()
                    .subjectCode(dept.getCode() + (semester * 100 + 3))
                    .subjectName("Applied " + dept.getCode() + " Technology")
                    .credits(3)
                    .subjectType(SubjectType.THEORY)
                    .semester(semester)
                    .weeklyPeriods(3)
                    .department(dept)
                    .build());

            Subject s4 = subjectRepository.save(Subject.builder()
                    .subjectCode(dept.getCode() + (semester * 100 + 4) + "L")
                    .subjectName(dept.getName() + " Laboratory")
                    .credits(2)
                    .subjectType(SubjectType.LAB)
                    .semester(semester)
                    .weeklyPeriods(3)
                    .department(dept)
                    .build());

            deptSubjects = List.of(s1, s2, s3, s4);
        }

        // 4. Create Faculty-Subject Mappings
        List<FacultySubjectMapping> newMappings = new ArrayList<>();
        for (int i = 0; i < deptSubjects.size(); i++) {
            Subject s = deptSubjects.get(i);
            Faculty f = deptFaculties.get(i % deptFaculties.size());
            FacultySubjectMapping m = FacultySubjectMapping.builder()
                    .faculty(f)
                    .subject(s)
                    .batch(batch)
                    .build();
            newMappings.add(mappingRepository.save(m));
        }

        return newMappings;
    }
}
