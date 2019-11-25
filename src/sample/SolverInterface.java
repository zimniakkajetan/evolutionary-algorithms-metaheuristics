package sample;

import java.util.HashMap;
import java.util.HashSet;

public interface SolverInterface {
    void run(double[][] matrix);

    HashMap<Integer, HashSet<Integer>> getGroups();

    double getPenalties();
}
