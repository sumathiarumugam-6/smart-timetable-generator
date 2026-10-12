package com.smarttimetable;

import com.smarttimetable.entity.*;
import com.smarttimetable.enums.DayOfWeekEnum;
import com.smarttimetable.enums.RoomType;
import com.smarttimetable.enums.SubjectType;
import com.smarttimetable.repository.*;
import com.smarttimetable.service.scheduler.SmartSchedulingEngine;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TimetableScenarioTest {

    @Autowired
    private SmartSchedulingEngine schedulingEngine;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ClassroomRepository classroomRepository;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private BatchRepository batchRepository;

    @Autowired
    private FacultySubjectMappingRepository mappingRepository;

    @Autowired
    private TimetableRepository timetableRepository;

    @Autowired
    private TimetableEntryRepository timetableEntryRepository;

    private static final int NUM_REPETITIONS = 3;

    @Test
    @Order(1)
    @DisplayName("Scenario 1: Baseline Single-Batch Curriculum (Standard Seed Data)")
    void testScenario1_BaselineSingleBatch() {
        System.out.println("\n================================================================================");
        System.out.println("SCENARIO 1: BASELINE SINGLE-BATCH CURRICULUM (STANDARD SEED DATA)");
        System.out.println("================================================================================");

        // Uses existing seed Department 1 (CSE, Semester 5, Batch 1: Sec A)
        Department dept = departmentRepository.findById(1L).orElseThrow();

        executeScenarioBenchmark(
                "Scenario 1 (Baseline CSE Sec A)",
                "2025-2026",
                5,
                dept,
                NUM_REPETITIONS,
                false // Expected to be conflict free
        );
    }

    @Test
    @Order(2)
    @DisplayName("Scenario 2: Dual-Section with Shared Faculty & Shared Lab")
    void testScenario2_DualBatchSharedFacultyAndLab() {
        System.out.println("\n================================================================================");
        System.out.println("SCENARIO 2: DUAL-SECTION WITH SHARED FACULTY & SHARED LAB");
        System.out.println("================================================================================");

        // Create isolated test department
        Department dept = departmentRepository.save(Department.builder()
                .code("TEST_S2")
                .name("Computer Science Sec A+B")
                .hodName("Dr. Ada Lovelace")
                .building("Turing Wing")
                .build());

        // Rooms: 2 Classrooms, 1 Laboratory
        Classroom c1 = classroomRepository.save(Classroom.builder()
                .roomNumber("S2-CR1")
                .building("Turing Wing")
                .capacity(70)
                .roomType(RoomType.CLASSROOM)
                .department(dept)
                .build());

        Classroom c2 = classroomRepository.save(Classroom.builder()
                .roomNumber("S2-CR2")
                .building("Turing Wing")
                .capacity(70)
                .roomType(RoomType.CLASSROOM)
                .department(dept)
                .build());

        Classroom lab1 = classroomRepository.save(Classroom.builder()
                .roomNumber("S2-LAB1")
                .building("Turing Wing")
                .capacity(40)
                .roomType(RoomType.LABORATORY)
                .department(dept)
                .build());

        // Faculty: 4 shared faculty
        Faculty f1 = facultyRepository.save(Faculty.builder()
                .facultyCode("FAC_S2_01")
                .name("Dr. Turing")
                .designation("Professor")
                .department(dept)
                .email("turing.s2@test.edu")
                .maxWorkingHours(20)
                .build());

        Faculty f2 = facultyRepository.save(Faculty.builder()
                .facultyCode("FAC_S2_02")
                .name("Prof. Hopper")
                .designation("Associate Professor")
                .department(dept)
                .email("hopper.s2@test.edu")
                .maxWorkingHours(20)
                .build());

        Faculty f3 = facultyRepository.save(Faculty.builder()
                .facultyCode("FAC_S2_03")
                .name("Prof. Knuth")
                .designation("Assistant Professor")
                .department(dept)
                .email("knuth.s2@test.edu")
                .maxWorkingHours(20)
                .build());

        Faculty f4 = facultyRepository.save(Faculty.builder()
                .facultyCode("FAC_S2_04")
                .name("Dr. Liskov")
                .designation("Assistant Professor")
                .department(dept)
                .email("liskov.s2@test.edu")
                .maxWorkingHours(20)
                .build());

        // Subjects: 2 CORE, 1 THEORY, 1 LAB
        Subject s1 = subjectRepository.save(Subject.builder()
                .subjectCode("CS201")
                .subjectName("Data Structures")
                .credits(4)
                .subjectType(SubjectType.CORE)
                .semester(5)
                .weeklyPeriods(4)
                .department(dept)
                .build());

        Subject s2 = subjectRepository.save(Subject.builder()
                .subjectCode("CS202")
                .subjectName("DBMS")
                .credits(4)
                .subjectType(SubjectType.CORE)
                .semester(5)
                .weeklyPeriods(4)
                .department(dept)
                .build());

        Subject s3 = subjectRepository.save(Subject.builder()
                .subjectCode("CS203")
                .subjectName("Operating Systems")
                .credits(3)
                .subjectType(SubjectType.THEORY)
                .semester(5)
                .weeklyPeriods(3)
                .department(dept)
                .build());

        Subject s4Lab = subjectRepository.save(Subject.builder()
                .subjectCode("CS204L")
                .subjectName("DBMS Lab")
                .credits(2)
                .subjectType(SubjectType.LAB)
                .semester(5)
                .weeklyPeriods(3)
                .department(dept)
                .build());

        // Batches: Section A and Section B
        Batch batchA = batchRepository.save(Batch.builder()
                .academicYear("2025-2026")
                .department(dept)
                .section("A")
                .semester(5)
                .studentCount(60)
                .build());

        Batch batchB = batchRepository.save(Batch.builder()
                .academicYear("2025-2026")
                .department(dept)
                .section("B")
                .semester(5)
                .studentCount(60)
                .build());

        // Mappings for Section A
        mappingRepository.save(FacultySubjectMapping.builder().faculty(f1).subject(s1).batch(batchA).preferredClassroom(c1).build());
        mappingRepository.save(FacultySubjectMapping.builder().faculty(f2).subject(s2).batch(batchA).preferredClassroom(c1).build());
        mappingRepository.save(FacultySubjectMapping.builder().faculty(f3).subject(s3).batch(batchA).preferredClassroom(c1).build());
        mappingRepository.save(FacultySubjectMapping.builder().faculty(f2).subject(s4Lab).batch(batchA).preferredClassroom(lab1).build());

        // Mappings for Section B (Shared Faculty f1, f2, f3 + shared lab1)
        mappingRepository.save(FacultySubjectMapping.builder().faculty(f1).subject(s1).batch(batchB).preferredClassroom(c2).build());
        mappingRepository.save(FacultySubjectMapping.builder().faculty(f2).subject(s2).batch(batchB).preferredClassroom(c2).build());
        mappingRepository.save(FacultySubjectMapping.builder().faculty(f3).subject(s3).batch(batchB).preferredClassroom(c2).build());
        mappingRepository.save(FacultySubjectMapping.builder().faculty(f4).subject(s4Lab).batch(batchB).preferredClassroom(lab1).build());

        executeScenarioBenchmark(
                "Scenario 2 (Dual Batch Sec A+B Shared Faculty/Lab)",
                "2025-2026",
                5,
                dept,
                NUM_REPETITIONS,
                false
        );
    }

    @Test
    @Order(3)
    @DisplayName("Scenario 3: Dense Curricular Load with Multiple Labs & Morning Core Competition")
    void testScenario3_DenseCurriculumMultipleLabs() {
        System.out.println("\n================================================================================");
        System.out.println("SCENARIO 3: DENSE CURRICULAR LOAD (7 SUBJECTS, 2 LABS, 25 PERIODS)");
        System.out.println("================================================================================");

        Department dept = departmentRepository.save(Department.builder()
                .code("TEST_S3")
                .name("Advanced Computing Dense Load")
                .hodName("Dr. John von Neumann")
                .building("Neumann Block")
                .build());

        Classroom c1 = classroomRepository.save(Classroom.builder()
                .roomNumber("S3-CR1")
                .building("Neumann Block")
                .capacity(70)
                .roomType(RoomType.CLASSROOM)
                .department(dept)
                .build());

        Classroom lab1 = classroomRepository.save(Classroom.builder()
                .roomNumber("S3-LAB1")
                .building("Neumann Block")
                .capacity(40)
                .roomType(RoomType.LABORATORY)
                .department(dept)
                .build());

        // 5 Faculty members
        List<Faculty> faculties = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            faculties.add(facultyRepository.save(Faculty.builder()
                    .facultyCode("FAC_S3_0" + i)
                    .name("Faculty S3-" + i)
                    .designation("Professor")
                    .department(dept)
                    .email("fac" + i + ".s3@test.edu")
                    .maxWorkingHours(24)
                    .build()));
        }

        // Subjects: 3 CORE (4p each = 12p), 2 THEORY (3p and 4p = 7p), 2 LABS (3p each = 6p) -> Total 25 periods
        Subject sub1Core = subjectRepository.save(Subject.builder().subjectCode("S3_C1").subjectName("Algorithms").credits(4).subjectType(SubjectType.CORE).semester(5).weeklyPeriods(4).department(dept).build());
        Subject sub2Core = subjectRepository.save(Subject.builder().subjectCode("S3_C2").subjectName("Databases").credits(4).subjectType(SubjectType.CORE).semester(5).weeklyPeriods(4).department(dept).build());
        Subject sub3Core = subjectRepository.save(Subject.builder().subjectCode("S3_C3").subjectName("Networks").credits(4).subjectType(SubjectType.CORE).semester(5).weeklyPeriods(4).department(dept).build());
        Subject sub4Th = subjectRepository.save(Subject.builder().subjectCode("S3_T1").subjectName("Software Engg").credits(3).subjectType(SubjectType.THEORY).semester(5).weeklyPeriods(4).department(dept).build());
        Subject sub5Th = subjectRepository.save(Subject.builder().subjectCode("S3_T2").subjectName("Cyber Security").credits(3).subjectType(SubjectType.THEORY).semester(5).weeklyPeriods(3).department(dept).build());
        Subject sub6Lab = subjectRepository.save(Subject.builder().subjectCode("S3_L1").subjectName("DBMS Lab").credits(2).subjectType(SubjectType.LAB).semester(5).weeklyPeriods(3).department(dept).build());
        Subject sub7Lab = subjectRepository.save(Subject.builder().subjectCode("S3_L2").subjectName("Networks Lab").credits(2).subjectType(SubjectType.LAB).semester(5).weeklyPeriods(3).department(dept).build());

        Batch batch = batchRepository.save(Batch.builder()
                .academicYear("2025-2026")
                .department(dept)
                .section("A")
                .semester(5)
                .studentCount(60)
                .build());

        mappingRepository.save(FacultySubjectMapping.builder().faculty(faculties.get(0)).subject(sub1Core).batch(batch).preferredClassroom(c1).build());
        mappingRepository.save(FacultySubjectMapping.builder().faculty(faculties.get(1)).subject(sub2Core).batch(batch).preferredClassroom(c1).build());
        mappingRepository.save(FacultySubjectMapping.builder().faculty(faculties.get(2)).subject(sub3Core).batch(batch).preferredClassroom(c1).build());
        mappingRepository.save(FacultySubjectMapping.builder().faculty(faculties.get(3)).subject(sub4Th).batch(batch).preferredClassroom(c1).build());
        mappingRepository.save(FacultySubjectMapping.builder().faculty(faculties.get(4)).subject(sub5Th).batch(batch).preferredClassroom(c1).build());
        mappingRepository.save(FacultySubjectMapping.builder().faculty(faculties.get(1)).subject(sub6Lab).batch(batch).preferredClassroom(lab1).build());
        mappingRepository.save(FacultySubjectMapping.builder().faculty(faculties.get(2)).subject(sub7Lab).batch(batch).preferredClassroom(lab1).build());

        executeScenarioBenchmark(
                "Scenario 3 (Dense Curriculum 25 Periods / 2 Labs)",
                "2025-2026",
                5,
                dept,
                NUM_REPETITIONS,
                false
        );
    }

    @Test
    @Order(4)
    @DisplayName("Scenario 4: Triple-Section Lab & Classroom Bottleneck Stress Test")
    void testScenario4_TripleSectionLabBottleneck() {
        System.out.println("\n================================================================================");
        System.out.println("SCENARIO 4: TRIPLE-SECTION LAB & CLASSROOM BOTTLENECK STRESS TEST (3 BATCHES, 1 LAB)");
        System.out.println("================================================================================");

        Department dept = departmentRepository.save(Department.builder()
                .code("TEST_S4")
                .name("High Contention Triple Section")
                .hodName("Dr. Claude Shannon")
                .building("Shannon Hall")
                .build());

        // 2 Theory rooms for 3 sections (Room Contention!)
        Classroom c1 = classroomRepository.save(Classroom.builder().roomNumber("S4-CR1").building("Shannon Hall").capacity(70).roomType(RoomType.CLASSROOM).department(dept).build());
        Classroom c2 = classroomRepository.save(Classroom.builder().roomNumber("S4-CR2").building("Shannon Hall").capacity(70).roomType(RoomType.CLASSROOM).department(dept).build());
        // ONLY 1 Lab for 3 distinct 3-period lab blocks (Lab Contention!)
        Classroom lab1 = classroomRepository.save(Classroom.builder().roomNumber("S4-LAB1").building("Shannon Hall").capacity(40).roomType(RoomType.LABORATORY).department(dept).build());

        // Shared faculties across 3 sections
        Faculty fCore1 = facultyRepository.save(Faculty.builder().facultyCode("FAC_S4_C1").name("Prof. Alpha").designation("Professor").department(dept).email("alpha.s4@test.edu").maxWorkingHours(24).build());
        Faculty fCore2 = facultyRepository.save(Faculty.builder().facultyCode("FAC_S4_C2").name("Prof. Beta").designation("Professor").department(dept).email("beta.s4@test.edu").maxWorkingHours(24).build());
        Faculty fTh = facultyRepository.save(Faculty.builder().facultyCode("FAC_S4_TH").name("Prof. Gamma").designation("Associate Professor").department(dept).email("gamma.s4@test.edu").maxWorkingHours(20).build());
        Faculty fLab = facultyRepository.save(Faculty.builder().facultyCode("FAC_S4_LB").name("Prof. Delta").designation("Assistant Professor").department(dept).email("delta.s4@test.edu").maxWorkingHours(20).build());

        Subject s1 = subjectRepository.save(Subject.builder().subjectCode("S4_C1").subjectName("Core One").credits(4).subjectType(SubjectType.CORE).semester(5).weeklyPeriods(4).department(dept).build());
        Subject s2 = subjectRepository.save(Subject.builder().subjectCode("S4_C2").subjectName("Core Two").credits(4).subjectType(SubjectType.CORE).semester(5).weeklyPeriods(4).department(dept).build());
        Subject s3 = subjectRepository.save(Subject.builder().subjectCode("S4_TH").subjectName("Theory Elec").credits(3).subjectType(SubjectType.THEORY).semester(5).weeklyPeriods(3).department(dept).build());
        Subject sLab = subjectRepository.save(Subject.builder().subjectCode("S4_LB").subjectName("Systems Lab").credits(2).subjectType(SubjectType.LAB).semester(5).weeklyPeriods(3).department(dept).build());

        // 3 Batches: A, B, C
        for (String sec : List.of("A", "B", "C")) {
            Batch batch = batchRepository.save(Batch.builder().academicYear("2025-2026").department(dept).section(sec).semester(5).studentCount(60).build());
            mappingRepository.save(FacultySubjectMapping.builder().faculty(fCore1).subject(s1).batch(batch).preferredClassroom(c1).build());
            mappingRepository.save(FacultySubjectMapping.builder().faculty(fCore2).subject(s2).batch(batch).preferredClassroom(c2).build());
            mappingRepository.save(FacultySubjectMapping.builder().faculty(fTh).subject(s3).batch(batch).preferredClassroom(c1).build());
            mappingRepository.save(FacultySubjectMapping.builder().faculty(fLab).subject(sLab).batch(batch).preferredClassroom(lab1).build());
        }

        executeScenarioBenchmark(
                "Scenario 4 (Triple Section 3 Labs in 1 Lab Room)",
                "2025-2026",
                5,
                dept,
                NUM_REPETITIONS,
                false
        );
    }

    @Test
    @Order(5)
    @DisplayName("Scenario 5: Mathematically Saturated / Over-Constrained Boundary Infeasibility")
    void testScenario5_OverConstrainedInfeasibleBoundary() {
        System.out.println("\n================================================================================");
        System.out.println("SCENARIO 5: OVER-CONSTRAINED INFEASIBLE BOUNDARY (7 LAB BLOCKS > 6 DAYS IN 1 LAB)");
        System.out.println("================================================================================");

        Department dept = departmentRepository.save(Department.builder()
                .code("TEST_S5")
                .name("Over-Constrained Boundary Dept")
                .hodName("Dr. Infeasible Boundary")
                .building("Boundary Block")
                .build());

        Classroom c1 = classroomRepository.save(Classroom.builder().roomNumber("S5-CR1").building("Boundary Block").capacity(70).roomType(RoomType.CLASSROOM).department(dept).build());
        Classroom lab1 = classroomRepository.save(Classroom.builder().roomNumber("S5-LAB1").building("Boundary Block").capacity(40).roomType(RoomType.LABORATORY).department(dept).build());

        // 7 Batches each demanding a 3-period continuous afternoon lab in the SAME SINGLE LAB ROOM
        // A week has only 6 days (Mon..Sat). 7 lab sessions cannot physically fit in 6 days!
        Subject sLab = subjectRepository.save(Subject.builder().subjectCode("S5_LAB").subjectName("Mandatory Lab").credits(2).subjectType(SubjectType.LAB).semester(5).weeklyPeriods(3).department(dept).build());

        for (int i = 1; i <= 7; i++) {
            Faculty f = facultyRepository.save(Faculty.builder().facultyCode("FAC_S5_" + i).name("Lab Faculty " + i).designation("Instructor").department(dept).email("lab" + i + ".s5@test.edu").maxWorkingHours(20).build());
            Batch b = batchRepository.save(Batch.builder().academicYear("2025-2026").department(dept).section("Sec_" + i).semester(5).studentCount(50).build());
            mappingRepository.save(FacultySubjectMapping.builder().faculty(f).subject(sLab).batch(b).preferredClassroom(lab1).build());
        }

        executeScenarioBenchmark(
                "Scenario 5 (Over-Constrained 7 Labs in 6-Day Single Room)",
                "2025-2026",
                5,
                dept,
                NUM_REPETITIONS,
                true // Expecting hard conflicts / infeasibility!
        );
    }

    private void executeScenarioBenchmark(
            String scenarioName,
            String academicYear,
            Integer semester,
            Department dept,
            int repetitions,
            boolean expectInfeasible
    ) {
        List<Long> runtimesMs = new ArrayList<>();
        List<Double> fitnessScores = new ArrayList<>();
        List<Integer> hardConflictsList = new ArrayList<>();
        List<Integer> softPenaltiesList = new ArrayList<>();

        Timetable lastGenerated = null;

        for (int run = 1; run <= repetitions; run++) {
            long startTime = System.nanoTime();
            Timetable timetable = schedulingEngine.generateTimetable(
                    scenarioName + " Run " + run,
                    academicYear,
                    semester,
                    dept,
                    null
            );
            long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;

            runtimesMs.add(elapsedMs);
            fitnessScores.add(timetable.getFitnessScore());
            hardConflictsList.add(timetable.getConflictCount());
            lastGenerated = timetable;

            System.out.printf("  Run %d/%d -> Runtime: %d ms | Fitness Score: %.2f | Hard Conflicts: %d | Status: %s%n",
                    run, repetitions, elapsedMs, timetable.getFitnessScore(), timetable.getConflictCount(), timetable.getStatus());
        }

        assertNotNull(lastGenerated, "Generated timetable must not be null");

        // Compute summary metrics
        long minRuntime = runtimesMs.stream().min(Long::compareTo).orElse(0L);
        long maxRuntime = runtimesMs.stream().max(Long::compareTo).orElse(0L);
        double avgRuntime = runtimesMs.stream().mapToLong(Long::longValue).average().orElse(0.0);

        System.out.println("\n  --- BENCHMARK SUMMARY FOR " + scenarioName + " ---");
        System.out.printf("  Execution Runtime: min = %d ms, max = %d ms, avg = %.2f ms%n", minRuntime, maxRuntime, avgRuntime);
        System.out.println("  Number of Generations: Unavailable (not exposed by GeneticAlgorithmScheduler or logs)");
        System.out.printf("  Hard Conflicts: %s%n", hardConflictsList);
        System.out.printf("  Fitness Scores: %s%n", fitnessScores);

        // Independent Verifications on the last generated timetable
        IndependentVerificationReport report = performIndependentVerification(lastGenerated);
        printVerificationReport(report);

        // Verify Database Persistence & Retrieval
        verifyDatabasePersistenceAndRetrieval(lastGenerated.getId());

        if (expectInfeasible) {
            System.out.println("  [EXPECTED INFEASIBILITY OBSERVED] Scenario was designed to exceed capacity.");
            assertTrue(report.totalHardConflicts > 0 || report.roomConflicts > 0,
                    "Scenario 5 must honestly detect and report room conflicts or hard conflicts due to 7 labs in 6 days!");
        } else {
            assertEquals(0, report.facultyConflicts, "Faculty conflicts must be 0 for feasible scenario");
            assertEquals(0, report.roomConflicts, "Classroom/Lab conflicts must be 0 for feasible scenario");
            assertEquals(0, report.batchConflicts, "Batch conflicts must be 0 for feasible scenario");
            assertTrue(report.labContinuousThreePeriods, "Lab continuous three periods must hold");
        }
    }

    private static class IndependentVerificationReport {
        int totalEntries;
        int facultyConflicts;
        int roomConflicts;
        int batchConflicts;
        int totalHardConflicts;
        boolean labContinuousThreePeriods;
        int brokenLabBlocks;
        int facultyWorkloadExceededDays; // Days where faculty > 6 periods
        int coreSubjectsTotal;
        int coreSubjectsMorning; // Periods 1-4
        int coreSubjectsAfternoon; // Periods 5-8
        double coreMorningCompliancePercent;
        int theorySubjectsChecked;
        int theorySubjectsUnderDistributed; // Appeared on < 3 days
    }

    private IndependentVerificationReport performIndependentVerification(Timetable timetable) {
        List<TimetableEntry> entries = timetableEntryRepository.findByTimetableId(timetable.getId());
        IndependentVerificationReport rep = new IndependentVerificationReport();
        rep.totalEntries = entries.size();

        // 1. Independent Faculty Conflicts: (day, period, facultyId)
        Map<String, List<TimetableEntry>> facultySlots = new HashMap<>();
        for (TimetableEntry e : entries) {
            if (e.getFaculty() != null && e.getFaculty().getId() != null) {
                String key = e.getDayOfWeek() + "_" + e.getPeriodNumber() + "_" + e.getFaculty().getId();
                facultySlots.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
            }
        }
        for (List<TimetableEntry> list : facultySlots.values()) {
            if (list.size() > 1) {
                rep.facultyConflicts += (list.size() - 1);
            }
        }

        // 2. Independent Classroom Conflicts: (day, period, classroomId)
        Map<String, List<TimetableEntry>> roomSlots = new HashMap<>();
        for (TimetableEntry e : entries) {
            if (e.getClassroom() != null && e.getClassroom().getId() != null) {
                String key = e.getDayOfWeek() + "_" + e.getPeriodNumber() + "_" + e.getClassroom().getId();
                roomSlots.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
            }
        }
        for (List<TimetableEntry> list : roomSlots.values()) {
            if (list.size() > 1) {
                rep.roomConflicts += (list.size() - 1);
            }
        }

        // 3. Independent Batch Conflicts: (day, period, batchId)
        Map<String, List<TimetableEntry>> batchSlots = new HashMap<>();
        for (TimetableEntry e : entries) {
            if (e.getBatch() != null && e.getBatch().getId() != null) {
                String key = e.getDayOfWeek() + "_" + e.getPeriodNumber() + "_" + e.getBatch().getId();
                batchSlots.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
            }
        }
        for (List<TimetableEntry> list : batchSlots.values()) {
            if (list.size() > 1) {
                rep.batchConflicts += (list.size() - 1);
            }
        }

        rep.totalHardConflicts = rep.facultyConflicts + rep.roomConflicts + rep.batchConflicts;

        // 4. Independent Continuous 3-Period Lab Allocation:
        // Group lab entries by (batchId, subjectId)
        Map<String, List<TimetableEntry>> labBlocks = new HashMap<>();
        for (TimetableEntry e : entries) {
            if (Boolean.TRUE.equals(e.getIsLabSession())) {
                String key = e.getBatch().getId() + "_" + e.getSubject().getId();
                labBlocks.computeIfAbsent(key, k -> new ArrayList<>()).add(e);
            }
        }

        rep.labContinuousThreePeriods = true;
        for (Map.Entry<String, List<TimetableEntry>> entry : labBlocks.entrySet()) {
            List<TimetableEntry> block = entry.getValue();
            if (block.size() != 3) {
                rep.labContinuousThreePeriods = false;
                rep.brokenLabBlocks++;
                continue;
            }
            DayOfWeekEnum day = block.get(0).getDayOfWeek();
            boolean sameDay = block.stream().allMatch(e -> e.getDayOfWeek() == day);
            List<Integer> periods = block.stream().map(TimetableEntry::getPeriodNumber).sorted().toList();
            boolean isContinuous567 = periods.equals(List.of(5, 6, 7));

            if (!sameDay || !isContinuous567) {
                rep.labContinuousThreePeriods = false;
                rep.brokenLabBlocks++;
            }
        }

        // 5. Independent Faculty Workload Limits (max 6 periods/day)
        Map<String, Integer> facultyDailyHours = new HashMap<>();
        for (TimetableEntry e : entries) {
            if (e.getFaculty() != null && e.getFaculty().getId() != null) {
                String key = e.getFaculty().getId() + "_" + e.getDayOfWeek();
                facultyDailyHours.put(key, facultyDailyHours.getOrDefault(key, 0) + 1);
            }
        }
        for (int count : facultyDailyHours.values()) {
            if (count > 6) {
                rep.facultyWorkloadExceededDays++;
            }
        }

        // 6. Independent Core-Subject Morning Preferences (Periods 1-4)
        for (TimetableEntry e : entries) {
            Subject s = e.getSubject();
            if (s != null && s.getSubjectType() == SubjectType.CORE) {
                rep.coreSubjectsTotal++;
                if (e.getPeriodNumber() <= 4) {
                    rep.coreSubjectsMorning++;
                } else {
                    rep.coreSubjectsAfternoon++;
                }
            }
        }
        rep.coreMorningCompliancePercent = rep.coreSubjectsTotal > 0
                ? ((double) rep.coreSubjectsMorning / rep.coreSubjectsTotal) * 100.0
                : 100.0;

        // 7. Independent Theory-Subject Multi-Day Distribution (>= 3 distinct days)
        Map<String, Set<DayOfWeekEnum>> theoryDays = new HashMap<>();
        for (TimetableEntry e : entries) {
            Subject s = e.getSubject();
            if (s != null && !Boolean.TRUE.equals(e.getIsLabSession())) {
                String key = e.getBatch().getId() + "_" + s.getId();
                theoryDays.computeIfAbsent(key, k -> new HashSet<>()).add(e.getDayOfWeek());
            }
        }
        rep.theorySubjectsChecked = theoryDays.size();
        for (Set<DayOfWeekEnum> days : theoryDays.values()) {
            if (days.size() < 3) {
                rep.theorySubjectsUnderDistributed++;
            }
        }

        return rep;
    }

    private void printVerificationReport(IndependentVerificationReport r) {
        System.out.println("  --- INDEPENDENT CONSTRAINT VERIFICATION ---");
        System.out.printf("  Total Slots / Entries Scheduled: %d%n", r.totalEntries);
        System.out.printf("  Faculty Double-Booking Conflicts: %d%n", r.facultyConflicts);
        System.out.printf("  Classroom / Lab Collision Conflicts: %d%n", r.roomConflicts);
        System.out.printf("  Batch / Section Double-Booking Conflicts: %d%n", r.batchConflicts);
        System.out.printf("  Total Hard Conflicts: %d%n", r.totalHardConflicts);
        System.out.printf("  Continuous 3-Period Lab Allocation: %s (Broken Blocks: %d)%n",
                r.labContinuousThreePeriods ? "VALID (All labs strictly 3 continuous afternoon periods P5-P7)" : "VIOLATED",
                r.brokenLabBlocks);
        System.out.printf("  Faculty Workload Limit (Max 6 periods/day): %s (%d daily violations)%n",
                r.facultyWorkloadExceededDays == 0 ? "PASSED (No faculty scheduled > 6 periods in a single day)" : "FLAGGED",
                r.facultyWorkloadExceededDays);
        System.out.printf("  Core Subject Morning Priority (Periods 1-4): %d/%d (%.1f%% Morning slots, %d Afternoon slots)%n",
                r.coreSubjectsMorning, r.coreSubjectsTotal, r.coreMorningCompliancePercent, r.coreSubjectsAfternoon);
        System.out.printf("  Theory Subject Multi-Day Distribution (>= 3 days): %d/%d subjects distributed across >= 3 days (%d under-distributed)%n",
                (r.theorySubjectsChecked - r.theorySubjectsUnderDistributed), r.theorySubjectsChecked, r.theorySubjectsUnderDistributed);
    }

    private void verifyDatabasePersistenceAndRetrieval(Long timetableId) {
        System.out.println("  --- DATABASE PERSISTENCE & RETRIEVAL VERIFICATION ---");
        Optional<Timetable> optTt = timetableRepository.findById(timetableId);
        assertTrue(optTt.isPresent(), "Timetable must be retrievable from database by ID: " + timetableId);

        Timetable retrieved = optTt.get();
        assertEquals(timetableId, retrieved.getId(), "Retrieved timetable ID must match");
        assertNotNull(retrieved.getTitle(), "Retrieved timetable title must not be null");
        assertNotNull(retrieved.getCreatedAt(), "Retrieved timetable createdAt must not be null");

        List<TimetableEntry> retrievedEntries = timetableEntryRepository.findByTimetableId(timetableId);
        assertFalse(retrievedEntries.isEmpty(), "Retrieved timetable must contain entries");

        for (TimetableEntry e : retrievedEntries) {
            assertNotNull(e.getId(), "TimetableEntry ID must be populated");
            assertNotNull(e.getDayOfWeek(), "Entry dayOfWeek must not be null");
            assertTrue(e.getPeriodNumber() >= 1 && e.getPeriodNumber() <= 8, "Entry periodNumber must be between 1 and 8");
            assertNotNull(e.getSubject(), "Entry subject must not be null");
            assertNotNull(e.getFaculty(), "Entry faculty must not be null");
            assertNotNull(e.getClassroom(), "Entry classroom must not be null");
            assertNotNull(e.getBatch(), "Entry batch must not be null");
        }

        System.out.printf("  Successfully retrieved Timetable ID %d from database with %d valid entries.%n",
                timetableId, retrievedEntries.size());
    }
}
