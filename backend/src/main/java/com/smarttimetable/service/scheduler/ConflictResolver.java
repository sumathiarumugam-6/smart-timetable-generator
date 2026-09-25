package com.smarttimetable.service.scheduler;

import com.smarttimetable.enums.DayOfWeekEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ConflictResolver {

    private final ConstraintChecker constraintChecker;

    public void resolveRemainingConflicts(Chromosome chromosome) {
        constraintChecker.evaluateFitness(chromosome);
        if (chromosome.getHardConflictCount() == 0) {
            return;
        }

        List<Gene> genes = chromosome.getGenes();
        DayOfWeekEnum[] days = DayOfWeekEnum.values();

        for (int i = 0; i < genes.size(); i++) {
            Gene g = genes.get(i);
            if (Boolean.TRUE.equals(g.getIsLabSession())) continue;

            // Try swapping to different day/period to reduce hard conflict count
            for (DayOfWeekEnum d : days) {
                for (int p = 1; p <= 8; p++) {
                    DayOfWeekEnum origDay = g.getDayOfWeek();
                    int origPeriod = g.getPeriodNumber();

                    g.setDayOfWeek(d);
                    g.setPeriodNumber(p);

                    Chromosome temp = new Chromosome(genes);
                    constraintChecker.evaluateFitness(temp);

                    if (temp.getHardConflictCount() < chromosome.getHardConflictCount()) {
                        chromosome.setHardConflictCount(temp.getHardConflictCount());
                        chromosome.setFitnessScore(temp.getFitnessScore());
                        if (chromosome.getHardConflictCount() == 0) return;
                    } else {
                        // Revert
                        g.setDayOfWeek(origDay);
                        g.setPeriodNumber(origPeriod);
                    }
                }
            }
        }
    }

    public boolean validateManualSwap(List<Gene> allEntries, Gene targetGene, DayOfWeekEnum newDay, int newPeriod, Long newClassroomId) {
        // Temporarily apply change
        DayOfWeekEnum oldDay = targetGene.getDayOfWeek();
        int oldPeriod = targetGene.getPeriodNumber();
        Long oldClassroom = targetGene.getClassroomId();

        targetGene.setDayOfWeek(newDay);
        targetGene.setPeriodNumber(newPeriod);
        if (newClassroomId != null) {
            targetGene.setClassroomId(newClassroomId);
        }

        Chromosome testChromosome = new Chromosome(allEntries);
        constraintChecker.evaluateFitness(testChromosome);

        boolean isValid = testChromosome.getHardConflictCount() == 0;

        // Revert
        targetGene.setDayOfWeek(oldDay);
        targetGene.setPeriodNumber(oldPeriod);
        targetGene.setClassroomId(oldClassroom);

        return isValid;
    }
}
