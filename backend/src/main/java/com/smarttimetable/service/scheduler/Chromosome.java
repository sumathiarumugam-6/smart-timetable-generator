package com.smarttimetable.service.scheduler;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Chromosome implements Comparable<Chromosome> {

    private List<Gene> genes;
    private double fitnessScore;
    private int hardConflictCount;
    private int softPenaltyCount;

    public Chromosome() {
        this.genes = new ArrayList<>();
        this.fitnessScore = 0.0;
        this.hardConflictCount = 0;
        this.softPenaltyCount = 0;
    }

    public Chromosome(List<Gene> genes) {
        this.genes = genes;
        this.fitnessScore = 0.0;
        this.hardConflictCount = 0;
        this.softPenaltyCount = 0;
    }

    public Chromosome cloneChromosome() {
        List<Gene> clonedGenes = new ArrayList<>();
        for (Gene g : genes) {
            clonedGenes.add(g.clone());
        }
        Chromosome clone = new Chromosome(clonedGenes);
        clone.setFitnessScore(this.fitnessScore);
        clone.setHardConflictCount(this.hardConflictCount);
        clone.setSoftPenaltyCount(this.softPenaltyCount);
        return clone;
    }

    @Override
    public int compareTo(Chromosome o) {
        // Higher fitness score comes first
        return Double.compare(o.fitnessScore, this.fitnessScore);
    }
}
