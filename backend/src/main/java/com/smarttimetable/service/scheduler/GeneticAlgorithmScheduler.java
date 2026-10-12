package com.smarttimetable.service.scheduler;

import com.smarttimetable.enums.DayOfWeekEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class GeneticAlgorithmScheduler {

    private final ConstraintChecker constraintChecker;
    private final Random random = new Random();

    public Chromosome runOptimization(List<Gene> initialTemplate, int populationSize, int maxGenerations) {
        if (initialTemplate.isEmpty()) {
            return new Chromosome();
        }

        List<Chromosome> population = initializePopulation(initialTemplate, populationSize);

        // Evaluate initial fitness
        for (Chromosome c : population) {
            constraintChecker.evaluateFitness(c);
        }
        Collections.sort(population);

        Chromosome bestOverall = population.get(0).cloneChromosome();

        for (int gen = 0; gen < maxGenerations; gen++) {
            if (bestOverall.getHardConflictCount() == 0 && bestOverall.getFitnessScore() > 95.0) {
                break; // Converged on zero conflict solution!
            }

            List<Chromosome> nextGen = new ArrayList<>();

            // Elitism: Retain top 10%
            int eliteCount = Math.max(1, populationSize / 10);
            for (int i = 0; i < eliteCount; i++) {
                nextGen.add(population.get(i).cloneChromosome());
            }

            // Reproduce remaining population
            while (nextGen.size() < populationSize) {
                Chromosome parent1 = tournamentSelect(population);
                Chromosome parent2 = tournamentSelect(population);

                Chromosome offspring = crossover(parent1, parent2);
                mutate(offspring);

                constraintChecker.evaluateFitness(offspring);
                nextGen.add(offspring);
            }

            population = nextGen;
            Collections.sort(population);

            if (population.get(0).getFitnessScore() > bestOverall.getFitnessScore()) {
                bestOverall = population.get(0).cloneChromosome();
            }
        }

        return bestOverall;
    }

    private List<Chromosome> initializePopulation(List<Gene> template, int popSize) {
        List<Chromosome> population = new ArrayList<>();
        DayOfWeekEnum[] days = DayOfWeekEnum.values();

        for (int i = 0; i < popSize; i++) {
            List<Gene> genes = new ArrayList<>();
            for (Gene t : template) {
                Gene g = t.clone();

                // Randomize Day and Period for initial variety (theory genes)
                if (!Boolean.TRUE.equals(g.getIsLabSession())) {
                    g.setDayOfWeek(days[random.nextInt(days.length)]);
                    g.setPeriodNumber(random.nextInt(8) + 1);
                }

                genes.add(g);
            }

            // For diverse initial population, randomize days of entire lab blocks across chromosomes
            if (i > 0) {
                Map<String, List<Gene>> labBlocks = new HashMap<>();
                for (Gene g : genes) {
                    if (Boolean.TRUE.equals(g.getIsLabSession())) {
                        String key = g.getBatchId() + "_" + g.getSubjectId();
                        labBlocks.computeIfAbsent(key, k -> new ArrayList<>()).add(g);
                    }
                }
                for (List<Gene> block : labBlocks.values()) {
                    DayOfWeekEnum newDay = days[random.nextInt(days.length)];
                    for (Gene g : block) {
                        g.setDayOfWeek(newDay);
                    }
                }
            }

            population.add(new Chromosome(genes));
        }
        return population;
    }

    private Chromosome tournamentSelect(List<Chromosome> population) {
        int tournamentSize = 5;
        Chromosome best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Chromosome competitor = population.get(random.nextInt(population.size()));
            if (best == null || competitor.getFitnessScore() > best.getFitnessScore()) {
                best = competitor;
            }
        }
        return best;
    }

    private Chromosome crossover(Chromosome p1, Chromosome p2) {
        List<Gene> offspringGenes = new ArrayList<>();
        List<Gene> g1 = p1.getGenes();
        List<Gene> g2 = p2.getGenes();

        Map<String, Boolean> labBlockParentChoice = new HashMap<>();

        for (int i = 0; i < g1.size(); i++) {
            Gene gene1 = g1.get(i);
            Gene gene2 = g2.get(i);

            if (Boolean.TRUE.equals(gene1.getIsLabSession())) {
                String blockKey = gene1.getBatchId() + "_" + gene1.getSubjectId();
                boolean fromP1 = labBlockParentChoice.computeIfAbsent(blockKey, k -> random.nextBoolean());
                offspringGenes.add(fromP1 ? gene1.clone() : gene2.clone());
            } else {
                if (random.nextBoolean()) {
                    offspringGenes.add(gene1.clone());
                } else {
                    offspringGenes.add(gene2.clone());
                }
            }
        }

        return new Chromosome(offspringGenes);
    }

    private void mutate(Chromosome chromosome) {
        double mutationRate = 0.15;
        DayOfWeekEnum[] days = DayOfWeekEnum.values();

        // 1. Mutate theory genes individually
        for (Gene g : chromosome.getGenes()) {
            if (random.nextDouble() < mutationRate && !Boolean.TRUE.equals(g.getIsLabSession())) {
                g.setDayOfWeek(days[random.nextInt(days.length)]);
                g.setPeriodNumber(random.nextInt(8) + 1);
            }
        }

        // 2. Block-based mutation for lab sessions: shift the entire block (P5, P6, P7) to a new day together
        Map<String, List<Gene>> labBlocks = new HashMap<>();
        for (Gene g : chromosome.getGenes()) {
            if (Boolean.TRUE.equals(g.getIsLabSession())) {
                String key = g.getBatchId() + "_" + g.getSubjectId();
                labBlocks.computeIfAbsent(key, k -> new ArrayList<>()).add(g);
            }
        }

        for (List<Gene> block : labBlocks.values()) {
            if (random.nextDouble() < mutationRate) {
                DayOfWeekEnum newDay = days[random.nextInt(days.length)];
                for (Gene g : block) {
                    g.setDayOfWeek(newDay);
                }
            }
        }
    }
}
