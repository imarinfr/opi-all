package org.lei.opi.core.definitions;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

/**
/* Hold and manipulate info and about frame
 *
 * @author Andrew Turpin
 * @date 5 June 2024
 */
public class FrameInfoImo extends FrameInfo {
    public static final int EYE_IMAGE_HEIGHT = 480;
    public static final int EYE_IMAGE_WIDTH = 640;

    /** Square to look for pupil about centre of camera image. Could be a bit smaller I reckon...  */
    private static final int MASK_RADIUS = 125;

    /** Current window for looking for pupil. Might change size as the stream progresses. */
    private static final Rect pupilRect = new Rect(EYE_IMAGE_WIDTH / 2 - MASK_RADIUS, EYE_IMAGE_HEIGHT / 2 - MASK_RADIUS,
                              MASK_RADIUS * 2, MASK_RADIUS * 2);

    /** Gaussian blur sigma  - from CREWt code  March 2013 */
    private static final double GAUSSIAN_BLUR_SIGMA = 1.8;

    /** Brightest a pupil can get for {@link detectPupil} */
    private static final int COLOR_UPPER_LIMIT = 80;

    /** Minimum area for a pupil in pixels */
    private static final int MIN_PUPIL_AREA = 100;

    /** Maximum area for a pupil in pixels */
    private static final int MAX_PUPIL_AREA = 6000;


    public FrameInfoImo() {
        super();
    }

    public FrameInfoImo(Mat m, long timeStamp) {
        super(m, timeStamp);
    }

    /**
     * Look for a pupil by in a central square region of {@link inputFrame} defined by {@ link pupilRect}.
     * Update {@link pupilX} etc as a side effect.
     *  1) Cut out pupilRect
     *  2) Gaussian blur
     *  3) Convert to Grey.
     *  4) Threshold with COLOR_UPPER_LIMIT
     *  5) Find all contours
     *  6) Filter contours by area and keep the "most circular" one.
     *
     * @param inputFrame An input image straight from camera. Should be EYE_IMAGE_WIDTH x EYE_IMAGE_HEIGHT
     */
    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    public void findPupil() {
        //System.out.println(" " + this.mat.size());
        //Imgcodecs.imwrite("input.jpg", this.mat);

        Mat inputROI = new Mat(this.mat, pupilRect);

        Imgproc.cvtColor(inputROI, inputROI, Imgproc.COLOR_RGB2GRAY, 0);

        Imgproc.GaussianBlur(inputROI, inputROI, new Size(0, 0), GAUSSIAN_BLUR_SIGMA);

        //Imgcodecs.imwrite("roi_b4Blobs.jpg", inputROI);

        Imgproc.threshold(inputROI, inputROI, COLOR_UPPER_LIMIT, 255, Imgproc.THRESH_BINARY);
        //Imgcodecs.imwrite("roi_thresh.jpg", inputROI);
        Imgproc.findContours(inputROI, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE, pupilRect.tl());

        /*
        Mat outputIm = new Mat();
        this.mat.copyTo(outputIm);
        Imgproc.drawContours(outputIm, contours, -1, new Scalar(0, 0, 255));
        for(MatOfPoint c : contours) {
            Rect r = Imgproc.boundingRect(c);
            double a = Imgproc.contourArea(c);
            double p = Imgproc.arcLength(new MatOfPoint2f(c.toArray()), true);
            double roundness = 4.0 * Math.PI * a / p / p;
            double myCircle = a / r.area();
            System.out.println(String.format("\nArea: %10.0f Perimeter: %8.2f Round: %6.4f MyRound: %6.4f", a, p, roundness, myCircle));
            if (roundness > 0.8)
                Imgproc.rectangle(outputIm, r.tl(), r.br(), new Scalar(0, 255, 0), 2);
        }
        Imgcodecs.imwrite("roi_conts.jpg", outputIm);
        */


            // Filter out dud contours with area outside limits and take
            // circular factor closest to pi/4.
        MatOfPoint bestContour = null;
        double closestDistance = 0;  // initialized to keep compiler happy
        for (MatOfPoint c : contours) {
            double a = Imgproc.contourArea(c);
            if (MIN_PUPIL_AREA < a && a < MAX_PUPIL_AREA) {
                Rect r = Imgproc.boundingRect(c);
                double circleDistance = Math.abs(a / r.area() - Math.PI / 4.0);
                if (bestContour == null || circleDistance < closestDistance) {
                    bestContour = c;
                    closestDistance = circleDistance;
                }
                else
                    c.release();
            }
        }

        if (bestContour == null || closestDistance > 0.2) {
            this.hasPupil = false;
        } else {
                // magic conversion factors from CREWt supplied code March 2013
            Moments m = Imgproc.moments(bestContour);
            this.pupilX = (m.m10 / m.m00 - EYE_IMAGE_WIDTH / 2.0) * 1.23;    // degrees
            this.pupilY = (m.m01 / m.m00 - EYE_IMAGE_HEIGHT / 2.0) * 1.23;   // degrees
            this.pupilDiameter = (2 * Math.sqrt(m.m00 / Math.PI)) * 14.0 / 176.0;  // mm
            this.hasPupil = true;

            bestContour.release();
        }

        inputROI.release();
        contours.clear();
        return;
    }
}