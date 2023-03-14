package org.lei.opi.core.definitions;

import java.util.ArrayList;
import java.util.function.Function;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * A class to hold information about what should be drawn on a Canvas to represent a visual field
 * at some stage in a test (a "data model").
 */
public class VFCanvas {
    /** List of points that are drawn when VFcanvas.draw(..., this) is called */
    private ArrayList<LabeledPoint> points;

    public VFCanvas() {
        this.points = new ArrayList<LabeledPoint>();
    }
    
    public ArrayList<LabeledPoint> getPoints() { return points;}

    /** 
     * @param x Centre of label on x-axis
     * @param y Centre of label on y-axis
     * @param label Text to write on canvas centered on (x, y)
    */
    public void addVFPoint(double x, double y, String label) {
        this.points.add(new LabeledPoint(x, y, label));
    }

    /**
     * Remove the closest point in the list to (x,y) by Euclidian distance
     * within a radius of 0.5 degrees-ish of (x,y).
     * @param x Centre of label on x-axis
     * @param y Centre of label on y-axis
     */
    public void removeVFPoint(double x, double y) {
        final double toleranceSq = 2.0 * 0.5 * 0.5;  // degrees
        double minDist = Double.MAX_VALUE;
        int minI = -1;
        for (int i = 0 ; i < this.points.size(); i++) {
            LabeledPoint p = this.points.get(i);
            double d = p.x() * p.x() + p.y() * p.y();
            if (d < minDist && d < toleranceSq) {
                minDist = d;
                minI = i;
            }
        }
        if (minI > -1)
            this.points.remove(minI);
    }

    /** 
     * Remove a label at (x,y) if it exists, and then put 
     * the new label in the list of points. 
     * @param x Centre of label on x-axis
     * @param y Centre of label on y-axis
     * @param label Text to write on canvas centered on (x, y)
     */

    public void updatePoint(double x, double y, String label) {
        this.removeVFPoint(x, y);
        this.addVFPoint(x, y, label);
    }

    //----------------- the redraw part (probably supposed to be in the "C" of MVC)

    static final private double vfSpan = 70.0; // total span of VF width and height in degrees
    static final private double border = 5.0; // pixels to not draw in around the edge of canvas
    static final private Color labelColor = Color.RED; // text on the canvas

    /**
     * All assumes (0, 0) degrees is at (w/2, h/2)
     *
     * @param canvas The javafx Canvas object on which to draw
     * @param model  A VFCanvas object with data that gets drawn on `canvas`.
     * 
     */
    public static void draw (Canvas canvas, VFCanvas model) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        Function<Double, Double> degreesToPixelsX = (x) -> { return w/2.0 + x / vfSpan * w; };
        Function<Double, Double> degreesToPixelsY = (y) -> { return h/2.0 - y / vfSpan * h; };  // VF y negative is down screen, which increases y

        gc.setFill(Color.WHITE);
        gc.fillRect(border, border, w - 2.0 * border, h - 2.0 * border);

            // draw grey grid assumes (0, 0) is at (w/2, h/2)
        gc.setStroke(Color.LIGHTGRAY);
        gc.setFill(Color.LIGHTGRAY);
        Font font = Font.font("Arial", 8);
        gc.setFont(font);
        for (double x = -27.0 ; x <= 27.0 ; x += 6.0) {
            long xp = Math.round(degreesToPixelsX.apply(x));
            long yp = Math.round(degreesToPixelsY.apply(x));
            gc.strokeLine(xp, border, xp, h - 2.0 * border);
            gc.strokeLine(border, yp, w - 2.0 * border, yp);
            Text text = new Text(Long.toString(Math.round(x)));  // text defaults to bottom left corner (x.y)
            text.setFont(font);
            if (xp > w / 2.0) {
                    // top and bottom to the left of "x"
                gc.fillText(Long.toString(Math.round(x)), xp - text.getLayoutBounds().getWidth(), border + text.getLayoutBounds().getHeight());  // top
                gc.fillText(Long.toString(Math.round(x)), xp - text.getLayoutBounds().getWidth(), h - 2.0 * border); // bottom
            } else {
                    // top and bottom to the right of x
                gc.fillText(Long.toString(Math.round(x)), xp, border + text.getLayoutBounds().getHeight());  // top
                gc.fillText(Long.toString(Math.round(x)), xp, h - 2.0 * border); // bottom
            }
            if (yp > h / 2.0) {
                    // left and right above y= -x 
                gc.fillText(Long.toString(Math.round(x)), border, yp);// left
                gc.fillText(Long.toString(Math.round(x)), w - 2.0 * border - text.getLayoutBounds().getWidth(), yp); // right
            } else {
                    // left and right below yp
                gc.fillText(Long.toString(Math.round(x)), border, yp + text.getLayoutBounds().getHeight());  // left
                gc.fillText(Long.toString(Math.round(x)), w - 2.0 * border - text.getLayoutBounds().getWidth(), yp + text.getLayoutBounds().getHeight()); // right
            }
        }

            // draw major axis (0, 0) is at (w/2, h/2)
        gc.setStroke(Color.BLACK);
        gc.strokeLine(w / 2.0, border, w / 2.0, h - border);
        gc.strokeLine(border, h / 2.0, w - border, h / 2.0);

            // draw point labels centered on their coordinates
        font = new Font("Arial", 10);
        gc.setFill(labelColor);
        gc.setFont(font);
        for (LabeledPoint p : model.getPoints()) {
            Text text = new Text(p.label());
            text.setFont(font);
            double x = degreesToPixelsX.apply(p.x()) - text.getLayoutBounds().getWidth() / 2.0;
            double y = degreesToPixelsY.apply(p.y()) + text.getLayoutBounds().getHeight() / 2.0;

            if (x >= border && x + text.getLayoutBounds().getWidth() / 2.0 <= w - border
            &&  y >= border && y + text.getLayoutBounds().getWidth() / 2.0 <= h - border)
                gc.fillText(p.label(), x, y);
        }
    }
}