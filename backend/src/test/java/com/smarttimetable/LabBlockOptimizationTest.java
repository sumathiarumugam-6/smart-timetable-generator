package com.smarttimetable;

import com.smarttimetable.enums.DayOfWeekEnum;
import com.smarttimetable.service.scheduler.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LabBlockOptimizationTest {

    @Test
    void testLabBlockMovesTogetherWithoutSplitting() {
        ConstraintChecker checker = new ConstraintChecker();
        ConflictResolver resolver = new ConflictResolver(checker);

        List<Gene> genes = new ArrayList<>();

        // Create a conflict on Monday:
        // Theory class on Monday Period 5, faculty 2, classroom 1, batch 1
        genes.add(Gene.builder()
                .dayOfWeek(DayOfWeekEnum.MONDAY)
                .periodNumber(5)
                .batchId(1L)
                .subjectId(1L)
                .facultyId(2L)
                .classroomId(1L)
                .isLabSession(false)
                .subjectType("CORE")
                .build());

        // Lab class also on Monday Periods 5, 6, 7 with SAME faculty 2!
        for (int p = 5; p <= 7; p++) {
            genes.add(Gene.builder()
                    .dayOfWeek(DayOfWeekEnum.MONDAY)
                    .periodNumber(p)
                    .batchId(1L)
                    .subjectId(4L)
                    .facultyId(2L) // Same faculty creates conflict on Monday Period 5
                    .classroomId(3L)
                    .isLabSession(true)
                    .subjectType("LAB")
                    .build());
        }

        Chromosome chromosome = new Chromosome(genes);
        checker.evaluateFitness(chromosome);
        assertTrue(chromosome.getHardConflictCount() > 0, "Initial chromosome should have hard conflicts");

        // Resolve conflicts
        resolver.resolveRemainingConflicts(chromosome);

        // Verify: conflict is resolved
        assertEquals(0, chromosome.getHardConflictCount(), "Conflicts should be fully resolved");

        // Verify lab block continuity: all 3 lab genes must have the EXACT SAME day and periods 5, 6, 7
        List<Gene> labGenes = chromosome.getGenes().stream()
                .filter(g -> Boolean.TRUE.equals(g.getIsLabSession()))
                .toList();
        assertEquals(3, labGenes.size(), "Should have exactly 3 lab genes");

        DayOfWeekEnum labDay = labGenes.get(0).getDayOfWeek();
        for (Gene lg : labGenes) {
            assertEquals(labDay, lg.getDayOfWeek(), "All lab genes must be on the same day");
        }

        List<Integer> periods = labGenes.stream().map(Gene::getPeriodNumber).sorted().toList();
        assertEquals(List.of(5, 6, 7), periods, "Lab periods must remain exactly 5, 6, 7");
    }
}
