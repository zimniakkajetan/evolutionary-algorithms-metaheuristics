package sample;

import java.util.*;

public class GreedyLocalSolver implements SolverInterface {
    /**
     * Assignment to group
     */
    private HashMap<Integer, HashSet<Integer>> groups;
    /**
     * Mean distance between connections
     */
    private double penalties;

    /**
     * Greedy Local Search solver
     *
     * @param groups Basic assignment to groups
     */
    public GreedyLocalSolver(HashMap<Integer, HashSet<Integer>> groups) {
        this.groups = new HashMap<>();
        for (Map.Entry<Integer, HashSet<Integer>> entry : groups.entrySet()) {
            HashSet<Integer> set = new HashSet<>(entry.getValue());
            this.groups.put(entry.getKey(), set);
        }
    }

    /**
     * Run calculations and prepare new assigns
     *
     * @param distanceMatrix Distances between points
     */
    public void run(double[][] distanceMatrix) {
        // Flag - penalties changed
        boolean penaltiesChanged = true;

        // Prepare basic penalties - reference
        Judge judge = new Judge();
        this.penalties = judge.calcMeanDistance(this.groups, distanceMatrix);

        while (penaltiesChanged) {
            penaltiesChanged = false;

            // Prepare potential moves
            ArrayList<int[]> moves = new ArrayList<>();
            for (Map.Entry<Integer, HashSet<Integer>> entry : this.groups.entrySet()) {
                for (Integer id : entry.getValue()) {
                    for (int groupId : this.groups.keySet()) {
                        if (groupId != entry.getKey()) {
                            int[] record = {id, entry.getKey(), groupId};
                            moves.add(record);
                        }
                    }
                }
            }

            // Random order
            Collections.shuffle(moves);

            // Apply move
            for (int[] move : moves) {
                judge.calculateChangedDistance(this.groups, move, distanceMatrix);

                // Move decremented current penalties - use it
                if (judge.tempMeanDistance() < this.penalties) {
                    // Save changes in groups
                    Integer pointID = move[0];
                    this.groups.get(move[1]).remove(pointID);
                    this.groups.get(move[2]).add(pointID);

                    // Apply changes in penalties
                    this.penalties = judge.updateDistance();

                    // Enable next iteration
                    penaltiesChanged = true;
                    break;
                }
            }
        }
    }

    /**
     * To use it, you should first call `calc` method.
     *
     * @return Prepared new groups
     */
    public HashMap<Integer, HashSet<Integer>> getGroups() {
        return this.groups;
    }

    /**
     * To use it, you should first call `calc` method.
     *
     * @return Penalties in assignment
     */
    public double getPenalties() {
        return penalties;
    }
}
