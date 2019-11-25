package sample;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;

public class Drawer {
    /**
     * Custom colors - different and easy to check on graph
     */
    private Color[] colors = {
            Color.RED,
            Color.AQUA,
            Color.BLUE,
            Color.GOLD,
            Color.BROWN,
            Color.CORAL,
            Color.ORANGE,
            Color.SILVER,
            Color.YELLOW,
            Color.VIOLET,
            Color.PURPLE,
            Color.SKYBLUE,
            Color.DEEPPINK,
            Color.SEAGREEN,
            Color.CADETBLUE,
            Color.AQUAMARINE,
            Color.BLUEVIOLET,
            Color.YELLOWGREEN,
            Color.SPRINGGREEN,
            Color.CORNFLOWERBLUE
    };

    /**
     * Draw instance as a graph
     *
     * @param coordinates Coordinates
     * @param groups      Solutions
     * @param title       Graph's title
     * @param isMST       Is MST graph flag
     * @param showLines   Should we show lines between points?
     */
    public void drawInputInstance(ArrayList<PointCoordinates> coordinates, HashSet<ArrayList<PointsPath>> groups, String title, boolean isMST, boolean showLines) {
        Pane root = new Pane();
        if (isMST) {
            Path path = new Path();
            path.setSmooth(true);

            // Draw lines on the graph
            int groupColorID = 0;
            for (ArrayList<PointsPath> group : groups) {
                Color selectedColor = colors[groupColorID];
                for (PointsPath route : group) {
                    PointCoordinates startPoint = coordinates.get(route.getStartIndex());
                    PointCoordinates endPoint = coordinates.get(route.getEndIndex());

                    // Move to start X, start Y
                    if (showLines) {
                        path.getElements().add(new MoveTo(startPoint.getX() * 2 + 20, startPoint.getY() * 2 + 20));
                    }
                    Circle startCircle = new Circle(2, selectedColor);
                    startCircle.relocate(startPoint.getX() * 2 + 18, startPoint.getY() * 2 + 18);
                    root.getChildren().add(startCircle);

                    // Add line to end X, end Y
                    if (showLines) {
                        path.getElements().add(new LineTo(endPoint.getX() * 2 + 20, endPoint.getY() * 2 + 20));
                    }
                    Circle endCircle = new Circle(2, selectedColor);
                    endCircle.relocate(endPoint.getX() * 2 + 18, endPoint.getY() * 2 + 18);
                    root.getChildren().add(endCircle);
                }

                groupColorID++;
            }

            // Prepare path on scene
            root.getChildren().add(path);
        } else {
            // Draw lines on the graph
            int groupColorID = 0;
            for (ArrayList<PointsPath> group : groups) {
                Color selectedColor = colors[groupColorID];
                for (PointsPath route : group) {
                    PointCoordinates startPoint = coordinates.get(route.getStartIndex());
                    PointCoordinates endPoint = coordinates.get(route.getEndIndex());

                    Line line = new Line(startPoint.getX() * 2 + 20, startPoint.getY() * 2 + 20, endPoint.getX() * 2 + 20, endPoint.getY() * 2 + 20);
                    line.setSmooth(true);
                    line.setStroke(selectedColor);
                    root.getChildren().add(line);

                    Circle startCircle = new Circle(2, Color.BLACK);
                    startCircle.relocate(startPoint.getX() * 2 + 18, startPoint.getY() * 2 + 18);
                    root.getChildren().add(startCircle);

                    Circle endCircle = new Circle(2, Color.BLACK);
                    endCircle.relocate(endPoint.getX() * 2 + 18, endPoint.getY() * 2 + 18);
                    root.getChildren().add(endCircle);
                }

                groupColorID++;
            }
        }

        // Title
        Label titleLabel = new Label();
        titleLabel.setText(title);
        titleLabel.relocate(2.0, 1.0);
        root.getChildren().add(titleLabel);

        // Set scene
        Stage stage = new Stage();
        Scene scene = new Scene(root, 600, 600);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.show();
    }
}

