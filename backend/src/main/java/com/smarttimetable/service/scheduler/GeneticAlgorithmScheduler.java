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

                // Randomize Day and Period for initial variety (preserving lab blocks if applicable)
                if (!Boolean.TRUE.equals(g.getIsLabSession())) {
                    g.setDayOfWeek(days[random.nextInt(days.length)]);
                    g.setPeriodNumber(random.nextInt(8) + 1);
                }

                genes.add(g);
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

        for (int i = 0; i < g1.size(); i++) {
            if (random.nextBoolean()) {
                offspringGenes.add(g1.get(i).clone());
            } else {
                offspringGenes.add(g2.get(i).clone());
            }
        }

        return new Chromosome(offspringGenes);
    }

    private void mutate(Chromosome chromosome) {
        double mutationRate = 0.15;
        DayOfWeekEnum[] days = DayOfWeekEnum.values();

        for (Gene g : chromosome.getGenes()) {
            if (random.nextDouble() < mutationRate && !Boolean.TRUE.equals(g.getIsLabSession())) {
                g.setDayOfWeek(days[random.nextInt(days.length)]);
                g.setPeriodNumber(random.nextInt(8) + 1);
            }
        }
    }
}
