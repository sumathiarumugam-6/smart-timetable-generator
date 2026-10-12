package com.smarttimetable;

import com.smarttimetable.enums.DayOfWeekEnum;
import com.smarttimetable.service.scheduler.Chromosome;
import com.smarttimetable.service.scheduler.ConstraintChecker;
import com.smarttimetable.service.scheduler.Gene;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConstraintCheckerTest {

    @Test
    void testLabSessionDoesNotReceiveMultiDayPenalty() {
        ConstraintChecker checker = new ConstraintChecker();
        List<Gene> genes = new ArrayList<>();

        // Add 3-period continuous lab on Monday (Periods 5, 6, 7)
        for (int p = 5; p <= 7; p++) {
            genes.add(Gene.builder()
                    .dayOfWeek(DayOfWeekEnum.MONDAY)
                    .periodNumber(p)
                    .batchId(1L)
                    .subjectId(4L) // Lab subject
                    .facultyId(2L)
                    .classroomId(3L)
                    .isLabSession(true)
                    .subjectType("LAB")
                    .build());
        }

        Chromosome chromosome = new Chromosome(genes);
        checker.evaluateFitness(chromosome);

        // Verify: hard conflicts = 0, soft penalties = 0 (no 20 point penalty for lab on 1 day!)
        assertEquals(0, chromosome.getHardConflictCount(), "Hard conflicts should be 0");
        assertEquals(0, chromosome.getSoftPenaltyCount(), "Soft penalties should be 0 for lab on 1 day");
    }

    @Test
    void testTheorySubjectStillReceivesMultiDayPenaltyIfOnLessThan3Days() {
        ConstraintChecker checker = new ConstraintChecker();
        List<Gene> genes = new ArrayList<>();

        // Add theory subject on only 1 day (e.g. 2 periods on Monday)
        for (int p = 1; p <= 2; p++) {
            genes.add(Gene.builder()
                    .dayOfWeek(DayOfWeekEnum.MONDAY)
                    .periodNumber(p)
                    .batchId(1L)
                    .subjectId(1L) // Theory subject
                    .facultyId(1L)
                    .classroomId(1L)
                    .isLabSession(false)
                    .subjectType("CORE")
                    .build());
        }

        Chromosome chromosome = new Chromosome(genes);
        checker.evaluateFitness(chromosome);

        // For theory subject on 1 day: (3 - 1) * 10 = 20 soft penalties
        assertEquals(20, chromosome.getSoftPenaltyCount(), "Theory subject on 1 day should incur 20 penalty points");
    }
}
