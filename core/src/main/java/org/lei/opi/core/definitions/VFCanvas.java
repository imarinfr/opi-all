package org.lei.opi.core.definitions;

import java.util.ArrayList;
import java.util.function.Function;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class VFCanvas {
    private ArrayList<LabeledPoint> points;

    public VFCanvas() {
        this.points = new ArrayList<LabeledPoint>();
    }
    
    public ArrayList<LabeledPoint> getPoints() { return points;}

    public void addVFPoint(double x, double y, String label) {
        this.points.add(new LabeledPoint(x, y, label));
    }

    /**
     * Remove the closest point in the list to (x,y) by Euclidian distance.
     */
    public void removeVFPoint(double x, double y, String label) {
        double minDist = Double.MAX_VALUE;
        int minI = -1;
        for (int i = 0 ; i < this.points.size(); i++) {
            LabeledPoint p = this.points.get(i);
            double d = Math.abs(p.x() * p.x() + p.y() * p.y());
            if (d < minDist) {
                minDist = d;
                minI = i;
            }
        }
        this.points.remove(minI);
    }

    //----------------- the redraw part (probably supposed to be in the "C" of MVC)
    static final private double pointRadius = 1.5; // degree of VF
    static final private double vfSpan = 62; // degrees of VF width and height
    static final private double border = 5; // pixels to not draw in around the edge

    public static void draw (Canvas canvas, VFCanvas model) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        Function<Double, Double> degreesToPixelsX = (x) -> { return w/2 + x / vfSpan * w; };
        Function<Double, Double> degreesToPixelsY = (y) -> { return h/2 + y / vfSpan * h; };

        gc.setFill(Color.WHITE);
        gc.fillRect(border, border, w - 2*border, h - 2*border);

            // draw grey grid (assumes square VF)
        gc.setStroke(Color.LIGHTGRAY);
        gc.setFill(Color.LIGHTGRAY);
        Font font = Font.font("Arial", 8);
        gc.setFont(font);
        for (double x = -30 ; x <= 30 ; x += 6) {
            gc.strokeLine(degreesToPixelsX.apply(x), border, degreesToPixelsX.apply(x), h - 2 * border);
            gc.strokeLine(border, degreesToPixelsY.apply(x), w - 2 * border, degreesToPixelsY.apply(x));
            Text text = new Text(Long.toString(Math.round(x)));  // text defaults to bottom left corner (x.y)
            text.setFont(font);
            if (x > 0) {
                    // top and bottom to the left of "x"
                gc.fillText(Long.toString(Math.round(x)), degreesToPixelsX.apply(x) - text.getLayoutBounds().getWidth(), border + text.getLayoutBounds().getHeight());  // top
                gc.fillText(Long.toString(Math.round(x)), degreesToPixelsX.apply(x) - text.getLayoutBounds().getWidth(), h - 2 * border); // bottom
                    // left and right above y= -x 
                gc.fillText(Long.toString(-Math.round(x)), border, degreesToPixelsX.apply(x));// left
                gc.fillText(Long.toString(-Math.round(x)), w - 2 * border - text.getLayoutBounds().getWidth(), degreesToPixelsY.apply(x)); // right
            } else {
                    // top and bottow to the right of x
                gc.fillText(Long.toString(Math.round(x)), degreesToPixelsX.apply(x), border + text.getLayoutBounds().getHeight());  // top
                gc.fillText(Long.toString(Math.round(x)), degreesToPixelsX.apply(x), h - 2 * border); // bottom
                    // left and right below y = -x
                gc.fillText(Long.toString(-Math.round(x)), border, degreesToPixelsX.apply(x) + text.getLayoutBounds().getHeight());  // left
                gc.fillText(Long.toString(-Math.round(x)), w - 2 * border - text.getLayoutBounds().getWidth(), degreesToPixelsY.apply(x) + text.getLayoutBounds().getHeight()); // right
            }
        }

            // draw major axis (again, square VF)
        gc.setStroke(Color.BLACK);
        gc.strokeLine(w / 2.0, border, w / 2.0, h - border);
        gc.strokeLine(border, h / 2.0, w - border, h / 2.0);

        gc.setFill(Color.RED);
        gc.setFont(new Font("Arial", 10));
        for (LabeledPoint p : model.getPoints()) {
            double x = degreesToPixelsX.apply(p.x() - pointRadius);
            double y = degreesToPixelsX.apply(p.y() - pointRadius);

            if (x >= border && x + 2 * pointRadius <= w - border
            &&  y >= border && y + 2 * pointRadius <= h - border)
                gc.fillText(p.label(), x, y);
                //gc.fillOval(x, y, pointRadius, pointRadius); 
        }
    }
}