package sample;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class PrimSolver {
    /**
     * Path as list of points
     */
    private ArrayList<PointsPath> path;

    /**
     * Get prepared MST as list of points
     *
     * @return MST list
     */
    public ArrayList<PointsPath> getPath() {
        return this.path;
    }

    /**
     * Construct MST and calculate penalties.
     *
     * @param indexes        Indexes of nodes in group
     * @param distanceMatrix Distance matrix to build structure and use distance
     */
    public void construct(int[] indexes, double[][] distanceMatrix) {
        this.path = new ArrayList<>();
        int pointsToVisit = indexes.length;
        int visitedPoints;
        HashSet<Integer> visitedIndexes = new HashSet<>();

        // Prepare list with all possible paths (fragments) between points
        ArrayList<PointsPath> possiblePaths = new ArrayList<>();
        for (int first = 0; first < pointsToVisit; first++) {
            int start = indexes[first];

            for (int second = first + 1; second < pointsToVisit; second++) {
                int end = indexes[second];
                possiblePaths.add(new PointsPath(start, end, distanceMatrix[start][end]));
            }
        }

        // Order by min distance first
        Collections.sort(possiblePaths);

        // Add first point to path list
        if (possiblePaths.isEmpty()) return;
        PointsPath point = possiblePaths.remove(0);
        this.path.add(point);
        visitedPoints = 2;
        visitedIndexes.add(point.getStartIndex());
        visitedIndexes.add(point.getEndIndex());

        while (visitedPoints != pointsToVisit) {
            for (int i = 0; i < possiblePaths.size(); i++) {
                // Get point from list
                PointsPath p = possiblePaths.get(i);

                // If start or end location in already visited - use it
                boolean containsFirst = visitedIndexes.contains(p.getStartIndex());
                boolean containsEnd = visitedIndexes.contains(p.getEndIndex());

                if ((containsFirst && !containsEnd) || (!containsFirst && containsEnd)) {
                    point = possiblePaths.remove(i);
                    this.path.add(point);
                    visitedIndexes.add(point.getStartIndex());
                    visitedIndexes.add(point.getEndIndex());

                    // Increment visited points and break for loop
                    visitedPoints += 1;
                    break;
                }
            }
        }
    }
}
