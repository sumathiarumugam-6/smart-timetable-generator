package com.smarttimetable.service.scheduler;

import com.smarttimetable.enums.DayOfWeekEnum;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ConstraintChecker {

    public void evaluateFitness(Chromosome chromosome) {
        int hardConflicts = 0;
        int softPenalties = 0;

        List<Gene> genes = chromosome.getGenes();

        // 1. Faculty Conflict & Room Conflict & Batch Conflict maps
        // Key: Day + Period + ResourceId
        Map<String, List<Gene>> facultySlots = new HashMap<>();
        Map<String, List<Gene>> roomSlots = new HashMap<>();
        Map<String, List<Gene>> batchSlots = new HashMap<>();

        // Maps for tracking daily workload per faculty
        // Key: FacultyId + Day -> Count
        Map<String, Integer> facultyDailyHours = new HashMap<>();

        // Track subject day occurrence
        // Key: BatchId + SubjectId -> Set of Days
        Map<String, Set<DayOfWeekEnum>> subjectDays = new HashMap<>();

        for (Gene g : genes) {
            if (g.getFacultyId() != null) {
                String fKey = g.getDayOfWeek() + "_" + g.getPeriodNumber() + "_" + g.getFacultyId();
                facultySlots.computeIfAbsent(fKey, k -> new ArrayList<>()).add(g);

                String fDayKey = g.getFacultyId() + "_" + g.getDayOfWeek();
                facultyDailyHours.put(fDayKey, facultyDailyHours.getOrDefault(fDayKey, 0) + 1);
            }

            if (g.getClassroomId() != null) {
                String rKey = g.getDayOfWeek() + "_" + g.getPeriodNumber() + "_" + g.getClassroomId();
                roomSlots.computeIfAbsent(rKey, k -> new ArrayList<>()).add(g);
            }

            if (g.getBatchId() != null) {
                String bKey = g.getDayOfWeek() + "_" + g.getPeriodNumber() + "_" + g.getBatchId();
                batchSlots.computeIfAbsent(bKey, k -> new ArrayList<>()).add(g);
            }

            if (g.getBatchId() != null && g.getSubjectId() != null && !Boolean.TRUE.equals(g.getIsLabSession())) {
                String bsKey = g.getBatchId() + "_" + g.getSubjectId();
                subjectDays.computeIfAbsent(bsKey, k -> new HashSet<>()).add(g.getDayOfWeek());
            }

            // Check Morning Priority for CORE subjects (Periods 1-4)
            if ("CORE".equalsIgnoreCase(g.getSubjectType()) && g.getPeriodNumber() > 4) {
                softPenalties += 5;
            }
        }

        // Evaluate Hard Conflicts
        for (Map.Entry<String, List<Gene>> entry : facultySlots.entrySet()) {
            if (entry.getValue().size() > 1) {
                hardConflicts += (entry.getValue().size() - 1) * 100;
            }
        }

        for (Map.Entry<String, List<Gene>> entry : roomSlots.entrySet()) {
            if (entry.getValue().size() > 1) {
                hardConflicts += (entry.getValue().size() - 1) * 100;
            }
        }

        for (Map.Entry<String, List<Gene>> entry : batchSlots.entrySet()) {
            if (entry.getValue().size() > 1) {
                hardConflicts += (entry.getValue().size() - 1) * 100;
            }
        }

        // Soft Constraint: Each faculty should have at least 2 free hours daily (max 6 periods out of 8 per day)
        for (Map.Entry<String, Integer> entry : facultyDailyHours.entrySet()) {
            if (entry.getValue() > 6) {
                softPenalties += (entry.getValue() - 6) * 15;
            }
        }

        // Soft Constraint: Subject should appear across multiple days (ideally 3 days)
        for (Map.Entry<String, Set<DayOfWeekEnum>> entry : subjectDays.entrySet()) {
            if (entry.getValue().size() < 3) {
                softPenalties += (3 - entry.getValue().size()) * 10;
            }
        }

        chromosome.setHardConflictCount(hardConflicts);
        chromosome.setSoftPenaltyCount(softPenalties);

        // Fitness score calculation (100 is perfect)
        double fitness = 100.0 / (1.0 + (hardConflicts * 10.0) + (softPenalties * 0.1));
        chromosome.setFitnessScore(fitness);
    }
}
