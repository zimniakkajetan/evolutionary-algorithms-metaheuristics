package sample;

import java.util.HashMap;
import java.util.HashSet;

public class Judge {
    /**
     * Total arcs in assignment
     */
    private int totalArcs;
    /**
     * Sum of distances - connections
     */
    private double sumOfDistances;
    /**
     * Sum of changes in connections. Can be negative and positive value.
     */
    private int changedArcs = 0;
    /**
     * Sum of changed (amount) distance
     */
    private double changedDistance = 0.0;

    /**
     * Calculate mean distance in connections. Sum of distances / total arcs
     *
     * @param groups         Assignment
     * @param distanceMatrix Distance matrix
     * @return Mean distance
     */
    public double calcMeanDistance(HashMap<Integer, HashSet<Integer>> groups, double[][] distanceMatrix) {
        this.sumOfDistances = 0.0;
        this.totalArcs = 0;

        for (HashSet<Integer> group : groups.values()) {
            int len = group.size();
            int[] indexes = group.stream().mapToInt(Integer::intValue).toArray();

            for (int i = 0; i < len; i++) {
                int ind_i = indexes[i];

                for (int j = i + 1; j < len; j++) {
                    this.totalArcs += 1;
                    this.sumOfDistances += distanceMatrix[ind_i][indexes[j]];
                }
            }
        }

        return this.sumOfDistances / (this.totalArcs > 0 ? this.totalArcs : 1);
    }

    /**
     * @param groups         Assignment
     * @param changes        Move, format [point ID, previous group ID, new group ID]
     * @param distanceMatrix Distance matrix
     */
    public void calculateChangedDistance(HashMap<Integer, HashSet<Integer>> groups, int[] changes, double[][] distanceMatrix) {
        this.changedDistance = 0.0;
        this.changedArcs = 0;

        // Point ID
        int pointID = changes[0];

        // Check current group
        int[] indexesInMyGroup = groups.get(changes[1]).stream().mapToInt(Integer::intValue).toArray();
        for (int index : indexesInMyGroup) {
            if (index != pointID) {
                this.changedArcs -= 1;
                this.changedDistance -= distanceMatrix[index][pointID];
            }
        }

        // Check new group
        int[] indexesInNewGroup = groups.get(changes[2]).stream().mapToInt(Integer::intValue).toArray();
        for (int index : indexesInNewGroup) {
            if (index != pointID) {
                this.changedArcs += 1;
                this.changedDistance += distanceMatrix[index][pointID];
            }
        }
    }

    /**
     * Update mean distance stored in Judge structure
     *
     * @return Mean distance after changes
     */
    public double updateDistance() {
        this.sumOfDistances += this.changedDistance;
        this.totalArcs += this.changedArcs;

        this.changedArcs = 0;
        this.changedDistance = 0.0;

        return this.sumOfDistances / (this.totalArcs > 0 ? this.totalArcs : 1);
    }

    /**
     * @return Changed distance (mean distance format)
     */
    public double tempMeanDistance() {
        int arcs = this.totalArcs + this.changedArcs;
        return (this.sumOfDistances + this.changedDistance) / (arcs > 0 ? arcs : 1);
    }

    public int getArcs() {
        return this.totalArcs + this.changedArcs;
    }

    public double getSumOfDistances() {
        return this.sumOfDistances + this.changedDistance;
    }
}