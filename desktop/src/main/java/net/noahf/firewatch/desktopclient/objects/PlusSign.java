package net.noahf.firewatch.desktopclient.objects;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class PlusSign extends Group {

    private final double size;

    public PlusSign(double size) {
        this.size = size;

        double radius = size / 2;
        Circle circle = new Circle(radius);
        circle.setFill(Color.BLACK);
        circle.setCenterX(radius);
        circle.setCenterY(radius);

        // Thickness and length of each bar in the plus sign
        double barThickness = size * 0.15;
        double barLength = size * 0.6;
        double barOffset = (size - barLength) / 2;
        double center = size / 2;

        // Horizontal bar
        Rectangle horizontal = new Rectangle(
                barOffset, center - barThickness / 2,
                barLength, barThickness
        );

        // Vertical bar
        Rectangle vertical = new Rectangle(
                center - barThickness / 2, barOffset,
                barThickness, barLength
        );

        // Union the two rectangles to form a plus
        Shape plus = Shape.union(horizontal, vertical);

        // Subtract the plus sign from the circle
        Shape cutout = Shape.subtract(circle, plus);

        getChildren().add(cutout);
    }

    public double getPlusSize() {
        return this.size;
    }
}