package net.noahf.firewatch.desktopclient.objects;


import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

public class Arrow extends Group {

    private final double size;

    public Arrow(double size) {
        this.size = size;

        Circle circle = new Circle(size / 2);
        circle.setFill(Color.BLACK);
        circle.setCenterX(size / 2);
        circle.setCenterY(size / 2);

        double arrowWidth = size * 0.4;
        double arrowHeight = size * 0.5;

        double x = (size - arrowWidth) / 2;
        double y = (size - arrowHeight) / 2;

        Polygon arrow = new Polygon(
                x + arrowWidth, y,                   // top right
                x + arrowWidth, y + arrowHeight,     // bottom right
                x, y + arrowHeight / 2               // tip (left)
        );

        // Subtract arrow from circle
        Shape cutout = Shape.subtract(circle, arrow);

        getChildren().add(cutout);
    }

    public double getArrowSize() {
        return this.size;
    }

}
