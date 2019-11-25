package sample;

import javafx.util.Pair;

import java.util.*;

public class EvolutionaryAlgorithm {
    /**
     * How many groups of points we should create
     */
    private int GROUPS;
    /**
     * Coordinates read-only copy
     */
    private final ArrayList<PointCoordinates> coordinates;
    /**
     * Start point read-only copy
     */
    private final ArrayList<Integer> startIndexesList;
    /**
     * How many solutions we will store in memory
     */
    private int HOW_MANY_SOLUTIONS_REMEMBER;
    /**
     * Memory with best solutions
     */
    private List<Pair<Double, HashMap<Integer, HashSet<Integer>>>> memory;

    /**
     * Evolutionary algorithm with memory
     *
     * @param coordinates        Points list
     * @param startIndexesList   Start indexes - assignment to groups
     * @param solutionsCacheSize Memory size
     */
    public EvolutionaryAlgorithm(ArrayList<PointCoordinates> coordinates, ArrayList<Integer> startIndexesList, int solutionsCacheSize, int groups) {
        this.HOW_MANY_SOLUTIONS_REMEMBER = solutionsCacheSize;
        this.memory = new ArrayList<>();
        this.coordinates = coordinates;
        this.startIndexesList = startIndexesList;
        this.GROUPS = groups;
    }

    /**
     * Run calculations and prepare new assigns
     *
     * @param distanceMatrix Distances between points
     * @param timeLimit      Time limit in nanoseconds
     */
    public void run(double[][] distanceMatrix, Long timeLimit) {
        long startTime = System.nanoTime();

        // 1. Prepare basic random solutions
        while (this.memory.size() < this.HOW_MANY_SOLUTIONS_REMEMBER) {
            HashMap<Integer, HashSet<Integer>> groupsOfPoints = this.randomInitGroups();
            SolverInterface solver = new SteepestLocalSolver(groupsOfPoints, false, 0, false);
            solver.run(distanceMatrix);
            this.addCandidateWhenBetter(solver.getPenalties(), solver.getGroups());
        }

        // 2. Prepare new solutions until you have time
        do {
            // 2A. Select parents
            Pair<Integer, Integer> parents = this.selectParents();

            // 2B. Prepare cross between both parents
            HashMap<Integer, HashSet<Integer>> groupsOfPoints = this.crossParents(parents);

            // 2C. Run local search
            SolverInterface solver = new SteepestLocalSolver(groupsOfPoints, false, 0, false);
            solver.run(distanceMatrix);

            // 2D. Add solution to memory
            this.addCandidateWhenBetter(solver.getPenalties(), solver.getGroups());
        } while (System.nanoTime() - startTime < timeLimit);
    }

    private HashMap<Integer, HashSet<Integer>> crossParents(Pair<Integer, Integer> parents) {
        HashMap<Integer, HashSet<Integer>> parentX = this.memory.get(parents.getKey()).getValue();
        HashMap<Integer, HashSet<Integer>> parentY = this.memory.get(parents.getValue()).getValue();

        // Prepare set with pairs in parent X
        Set<Pair<Integer, Integer>> elementsInParentX = new HashSet<>();
        for (Map.Entry<Integer, HashSet<Integer>> entry : parentX.entrySet()) {
            for (Integer pointX : entry.getValue()) {
                for (Integer pointY : entry.getValue()) {
                    if (!pointX.equals(pointY)) {
                        elementsInParentX.add(new Pair<>(pointX, pointY));
                    }
                }
            }
        }

        // Prepare set with pairs in parent Y
        Set<Pair<Integer, Integer>> elementsInParentY = new HashSet<>();
        for (Map.Entry<Integer, HashSet<Integer>> entry : parentY.entrySet()) {
            for (Integer pointX : entry.getValue()) {
                for (Integer pointY : entry.getValue()) {
                    if (!pointX.equals(pointY)) {
                        elementsInParentY.add(new Pair<>(pointX, pointY));
                    }
                }
            }
        }

        // Prepare intersection
        Set<Pair<Integer, Integer>> intersection = new HashSet<>(elementsInParentX);
        intersection.retainAll(elementsInParentY);

        // Prepare basic assignment to groups based on intersection
        HashMap<Integer, HashSet<Integer>> groupsWithPoints = new HashMap<>();
        for (int i = 0; i < GROUPS; i++) {
            groupsWithPoints.put(i, new HashSet<>());
        }

        Random random = new Random();
        HashSet<Integer> usedPoints = new HashSet<>();

        for (Pair<Integer, Integer> points : intersection) {
            Integer x = points.getKey();
            Integer y = points.getValue();
            boolean added = false;

            for (int i = 0; i < GROUPS; i++) {
                if (groupsWithPoints.get(i).contains(x)) {
                    groupsWithPoints.get(i).add(y);
                    added = true;
                    break;
                }
            }

            if (!added) {
                int groupID = random.nextInt(GROUPS);
                groupsWithPoints.get(groupID).add(x);
                groupsWithPoints.get(groupID).add(y);
            }

            usedPoints.add(x);
            usedPoints.add(y);
        }

        HashSet<Integer> emptyGroups = new HashSet<>();
        for (Map.Entry<Integer, HashSet<Integer>> entry : groupsWithPoints.entrySet()) {
            if (entry.getValue().isEmpty()) {
                emptyGroups.add(entry.getKey());
            }
        }

        // We need add missing points - through random
        int selectedGroupID;
        for (PointCoordinates point : this.coordinates) {
            if (!usedPoints.contains(point.getID())) {
                if (emptyGroups.isEmpty()) {
                    selectedGroupID = random.nextInt(GROUPS);
                } else {
                    selectedGroupID = emptyGroups.iterator().next();
                    emptyGroups.remove(selectedGroupID);
                }

                groupsWithPoints.get(selectedGroupID).add(point.getID());
            }
        }

        return groupsWithPoints;
    }

    private Pair<Integer, Integer> selectParents() {
        Random random = new Random();
        Integer parentX = random.nextInt(this.HOW_MANY_SOLUTIONS_REMEMBER);
        Integer parentY;
        do {
            parentY = random.nextInt(this.HOW_MANY_SOLUTIONS_REMEMBER);
        } while (parentX.equals(parentY));

        return new Pair<>(parentX, parentY);
    }

    private void addCandidateWhenBetter(double penalties, HashMap<Integer, HashSet<Integer>> groups) {
        // Check contain already this value
        boolean match = this.memory.stream().map(Pair::getKey).anyMatch(x -> x == penalties);
        if (!match) {
            // Sort by key (penalties)
            this.memory.sort(Comparator.comparing(Pair::getKey));

            // If already full
            if (this.memory.size() >= this.HOW_MANY_SOLUTIONS_REMEMBER) {
                // When penalties less than last value
                if (penalties < this.memory.get(this.HOW_MANY_SOLUTIONS_REMEMBER - 1).getKey()) {
                    this.memory.remove(this.HOW_MANY_SOLUTIONS_REMEMBER - 1);

                    // Add new element
                    this.memory.add(new Pair<>(penalties, groups));
                }
            } else {
                // Add new element
                this.memory.add(new Pair<>(penalties, groups));
            }
        }
    }

    private HashMap<Integer, HashSet<Integer>> randomInitGroups() {
        HashMap<Integer, HashSet<Integer>> groupsWithPoints = new HashMap<>();
        for (Map.Entry<Integer, HashSet<Integer>> entry : Main.randomInitGroups(this.startIndexesList, this.coordinates).entrySet()) {
            HashSet<Integer> set = new HashSet<>(entry.getValue());
            groupsWithPoints.put(entry.getKey(), set);
        }

        return groupsWithPoints;
    }

    public double getBestPenalties() {
        // Sort by key (penalties)
        this.memory.sort(Comparator.comparing(Pair::getKey));

        // Return value stored in first pair
        return this.memory.get(0).getKey();
    }

    public HashMap<Integer, HashSet<Integer>> getBestGroups() {
        // Sort by key (penalties)
        this.memory.sort(Comparator.comparing(Pair::getKey));

        // Return assignment stored in with pair
        return this.memory.get(0).getValue();

    }
}