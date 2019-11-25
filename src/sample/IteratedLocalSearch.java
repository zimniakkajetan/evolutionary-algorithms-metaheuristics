package sample;

import javafx.util.Pair;

import java.util.*;

public class IteratedLocalSearch {
    /**
     * How many points should be moved in perturbation
     */
    private int PERTURBATION_CHANGES_NUMBER;
    /**
     * Which version of perturbation we should use. Small when True
     */
    private MODE PERTURBATION_MODE;
    /**
     * Assignment to group
     */
    private HashMap<Integer, HashSet<Integer>> groups;
    private HashMap<Integer, HashSet<Integer>> bestGroups;
    /**
     * Mean distance between connections
     */
    private double bestPenalties;

    /**
     * Iterated local search solver
     *
     * @param groups           Basic assignment to groups
     * @param perturbationMode Perturbation mode
     */
    public IteratedLocalSearch(HashMap<Integer, HashSet<Integer>> groups, MODE perturbationMode) {
        this.bestGroups = new HashMap<>();
        this.bestPenalties = Double.MAX_VALUE;
        this.PERTURBATION_MODE = perturbationMode;

        int numberOfPoints = 0;
        for (Map.Entry<Integer, HashSet<Integer>> entry : groups.entrySet()) {
            HashSet<Integer> set = new HashSet<>(entry.getValue());
            numberOfPoints += set.size();
            this.bestGroups.put(entry.getKey(), set);
        }

        if (this.PERTURBATION_MODE == MODE.SMALL) {
            this.PERTURBATION_CHANGES_NUMBER = (int) (numberOfPoints * 0.02) + 1;
        } else {
            this.PERTURBATION_CHANGES_NUMBER = (int) (numberOfPoints * 0.3) + 1;
        }
    }

    /**
     * Run calculations and prepare new assigns
     *
     * @param distanceMatrix Distances between points
     * @param timeLimit      Time limit in nanoseconds
     */
    public void run(double[][] distanceMatrix, Long timeLimit) {
        long startTime = System.nanoTime();
        do {
            long time = System.nanoTime();
            //init start groups instance
            this.groups = new HashMap<>();
            for (Map.Entry<Integer, HashSet<Integer>> entry : this.bestGroups.entrySet()) {
                HashSet<Integer> set = new HashSet<>(entry.getValue());
                this.groups.put(entry.getKey(), set);
            }

            if (this.PERTURBATION_MODE == MODE.SMALL) {
                smallGroupPerturbation();
            } else if (this.PERTURBATION_MODE == MODE.BIG_RANDOM) {
                bigRandomPerturbation(distanceMatrix);
            } else {
                bigHeuristicPerturbation(distanceMatrix);
            }

            SteepestLocalSolver randomSteepestLocalSolver = new SteepestLocalSolver(this.groups, false, 0, false);
            randomSteepestLocalSolver.run(distanceMatrix);
            double penalties = randomSteepestLocalSolver.getPenalties();
            this.groups = randomSteepestLocalSolver.getGroups();

            if (penalties < this.bestPenalties) {
                this.bestPenalties = penalties;
                this.bestGroups = this.groups;
            }

        } while (System.nanoTime() - startTime < timeLimit);
    }

    /**
     * Execute after assign global best groups to groups usung in single iteration when isSmallPerturbation == true
     */
    private void smallGroupPerturbation() {
        Random rand = new Random();
        int startRandomGroupId, targetRandomGroupId;

        for (int i = 0; i < PERTURBATION_CHANGES_NUMBER; i++) {
            // Get a random group
            do {
                startRandomGroupId = rand.nextInt(this.groups.size());
                targetRandomGroupId = rand.nextInt(this.groups.size());
            } while (startRandomGroupId == targetRandomGroupId || this.groups.get(startRandomGroupId).size() == 1);

            // Get point ID
            Integer pointID = this.groups.get(startRandomGroupId).iterator().next();
            this.groups.get(startRandomGroupId).remove(pointID);
            this.groups.get(targetRandomGroupId).add(pointID);
        }
    }

    /**
     * Big perturbation with two stage: destroy and repair
     */
    private void bigRandomPerturbation(double[][] distanceMatrix) {
        List<Integer> destroyedPoints = new ArrayList<>();
        Random rand = new Random();
        int randomGroupId, selectedGroupIndex = 0;
        double minDistanceValue, distance;

        // Destroy
        for (int i = 0; i < PERTURBATION_CHANGES_NUMBER; i++) {
            do {
                randomGroupId = rand.nextInt(this.groups.size());
            } while (this.groups.get(randomGroupId).size() == 1);

            Integer pointID = this.groups.get(randomGroupId).iterator().next();
            this.groups.get(randomGroupId).remove(pointID);
            destroyedPoints.add(pointID);
        }

        // Repair
        for (Integer pointID : destroyedPoints) {
            minDistanceValue = Double.MAX_VALUE;

            // Find group with minimum average distance between each point in group and current point
            for (Map.Entry<Integer, HashSet<Integer>> entry : this.groups.entrySet()) {
                distance = 0.0;

                for (Integer neighborPointID : entry.getValue()) {
                    distance += distanceMatrix[neighborPointID][pointID];
                }

                // Check distance is smaller than current stored - if yes => update index
                if (distance < minDistanceValue) {
                    minDistanceValue = distance;
                    selectedGroupIndex = entry.getKey();
                }
            }

            // Add point to selected group
            this.groups.get(selectedGroupIndex).add(pointID);
        }
    }

    /**
     * Big perturbation with two stage: destroy and repair.
     * Destroy based on heuristic
     */
    private void bigHeuristicPerturbation(double[][] distanceMatrix) {
        List<Integer> destroyedPoints = new ArrayList<>();
        int selectedGroupIndex = 0, elementsCount;
        double minDistanceValue, distance;

        // Destroy
        ArrayList<Pair<Pair<Integer, Integer>, Integer>> points = new ArrayList<>();
        for (Map.Entry<Integer, HashSet<Integer>> entry : this.groups.entrySet()) {
            for (Integer pointID : entry.getValue()) {
                distance = 0.0;
                elementsCount = 0;
                for (Integer neighborPointID : entry.getValue()) {
                    if (!pointID.equals(neighborPointID)) {
                        distance += distanceMatrix[neighborPointID][pointID];
                        elementsCount++;
                    }
                }

                points.add(new Pair<>(new Pair<>(pointID, entry.getKey()), (int) (-distance * 1_000 / elementsCount)));
            }
        }

        points.sort(Comparator.comparingInt(Pair::getValue));

        for (int i = 0; i < PERTURBATION_CHANGES_NUMBER; i++) {
            Pair<Integer, Integer> pair = points.get(i).getKey();
            this.groups.get(pair.getValue()).remove(pair.getKey());
            destroyedPoints.add(pair.getKey());
        }

        // Repair
        for (Integer pointID : destroyedPoints) {
            minDistanceValue = Double.MAX_VALUE;

            // Find group with minimum distance between each point in group and current point
            for (Map.Entry<Integer, HashSet<Integer>> entry : this.groups.entrySet()) {
                distance = 0.0;
                elementsCount = 0;

                for (Integer neighborPointID : entry.getValue()) {
                    distance += distanceMatrix[neighborPointID][pointID];
                    elementsCount++;
                }

                // Check distance is smaller than current stored - if yes => update index
                if ((distance / elementsCount) < minDistanceValue) {
                    minDistanceValue = (distance / elementsCount);
                    selectedGroupIndex = entry.getKey();
                }
            }

            // Add point to selected group
            this.groups.get(selectedGroupIndex).add(pointID);
        }
    }

    /**
     * To use it, you should first call `calc` method.
     *
     * @return Prepared best groups
     */
    public HashMap<Integer, HashSet<Integer>> getBestGroups() {
        return this.bestGroups;
    }

    /**
     * To use it, you should first call `calc` method.
     *
     * @return Best penalties in assignment
     */
    public double getBestPenalties() {
        return this.bestPenalties;
    }

    /**
     * Perturbation mode - supported SMALL, BIG_RANDOM and BIG_HEURISTIC
     */
    public enum MODE {
        SMALL,
        BIG_RANDOM,
        BIG_HEURISTIC
    }
}

