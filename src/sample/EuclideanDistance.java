package sample;

import java.util.ArrayList;

public class EuclideanDistance {
    /**
     * Calculate distance between points and store it as distance matrix
     *
     * @param points List of points
     * @return Distance matrix row x column
     */
    public double[][] calculateDistanceMatrix(ArrayList<PointCoordinates> points) {
        int numberOfPoints = points.size();
        double[][] distanceMatrix = new double[numberOfPoints][numberOfPoints];

        int row = 0;
        for (PointCoordinates firstPoint : points) {
            int column = 0;
            for (PointCoordinates secondPoint : points) {
                distanceMatrix[row][column] = Math.sqrt(Math.pow(firstPoint.getX() - secondPoint.getX(), 2) +
                        Math.pow(firstPoint.getY() - secondPoint.getY(), 2));
                column++;
            }
            row++;
        }

        return distanceMatrix;
    }
}
