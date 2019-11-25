package sample;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Reader {

    /**
     * Read instance and parse coordinates to list of points
     *
     * @param fileName Instance file path
     * @return Array of points (PointCoordinates)
     */
    public ArrayList<PointCoordinates> readInstance(String fileName) {
        ArrayList<PointCoordinates> points = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            String line;
            int index = 0;

            // Read file line by line
            while ((line = in.readLine()) != null) {
                String[] coordinates = line.split("\\s");
                int x = Integer.parseInt(coordinates[0]);
                int y = Integer.parseInt(coordinates[1]);

                // Add new point with ID
                points.add(new PointCoordinates(index, x, y));

                // Index as next ID
                index++;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return points;
    }
}
