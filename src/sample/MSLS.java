package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MSLS {
    private final ArrayList<Integer> startIndexesList;
    private final ArrayList<PointCoordinates> coordinates;
    private int iterationsOfLocalSearch;
    private double[][] distanceMatrix;
    private double minPenalties;
    private HashMap<Integer, HashSet<Integer>> bestGroups;
    private int mode;

    public MSLS(int mode, int iterationsOfLocalSearch, ArrayList<Integer> startIndexesList, double[][] distanceMatrix, ArrayList<PointCoordinates> coordinates) {
        this.mode = mode;
        this.iterationsOfLocalSearch = iterationsOfLocalSearch;
        this.distanceMatrix = distanceMatrix;
        this.startIndexesList = startIndexesList;
        this.coordinates = coordinates;
    }

    public void run() {
        this.minPenalties = Double.MAX_VALUE;
        this.bestGroups = new HashMap<>();

        for (int iteration = 0; iteration < this.iterationsOfLocalSearch; iteration++) {
            HashMap<Integer, HashSet<Integer>> groupsOfPoints = this.randomInitGroups();
            SolverInterface solver;
            if (this.mode == 0) {
                solver = new GreedyLocalSolver(groupsOfPoints);
            } else {
                solver = new SteepestLocalSolver(groupsOfPoints, false, 20, false);
            }

            solver.run(this.distanceMatrix);
            if (solver.getPenalties() < this.minPenalties) {
                this.minPenalties = solver.getPenalties();
                this.bestGroups = solver.getGroups();
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

    public double getMinPenalties() {
        return minPenalties;
    }

    public HashMap<Integer, HashSet<Integer>> getGroups() {
        return bestGroups;
    }
}
