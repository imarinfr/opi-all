package org.lei.opi.core.definitions;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.features2d.SimpleBlobDetector_Params;
import org.opencv.imgproc.Imgproc;

/**
/* Hold and manipulate info and about frame 
 *
 * @author Andrew Turpin
 * @date 5 June 2024 
 */
public class FrameInfoImo extends FrameInfo {
    public static final int EYE_IMAGE_HEIGHT = 480;
    public static final int EYE_IMAGE_WIDTH = 640;

    /** Square to look for pupil about centre of camera image  */
    private static final int MASK_RADIUS = 125;

    /** Brightest a pupil can get for {@link detectPupil} */
    private static final int COLOR_UPPER_LIMIT = 80;

    private static final SimpleBlobDetector_Params params = new SimpleBlobDetector_Params();
    private static SimpleBlobDetector blobDetector;

    /** Current window for looking for pupil. Might change size as the stream progresses. */
    private static final Rect pupilRect = new Rect(EYE_IMAGE_WIDTH / 2 - MASK_RADIUS, EYE_IMAGE_HEIGHT / 2 - MASK_RADIUS, 
                              MASK_RADIUS * 2, MASK_RADIUS * 2);


    public FrameInfoImo() {
        super();
        params.set_filterByArea(true);
        params.set_filterByCircularity(true);
        params.set_minThreshold(0);
        params.set_maxThreshold(COLOR_UPPER_LIMIT);
        params.set_minArea(100);
        params.set_maxArea(6000);
        params.set_minRepeatability(2);
        blobDetector = SimpleBlobDetector.create(params);
    }

    public FrameInfoImo(Mat m, long timeStamp) {
        super(m, timeStamp);
    }

    /**
     * Look for a pupil ellipse in a central square region of {@link inputFrame} defined by {@ link pupilRect}.
     * Update {@link pupilInfo} as a side effect.
     *  1) Cut out pupilRect
     *  2) Gaussian blur
     *  3) Convert to Grey.
     *  4) Find circular blobs
     *  5) If there's more than one, get the darkest one.
     *
     * @param inputFrame An input image straight from camera. Should be EYE_IMAGE_WIDTH x EYE_IMAGE_HEIGHT
     */
    public void findPupil() {
        //System.out.println(" " + inputFrame.size());
        //Imgcodecs.imwrite("input.jpg", inputFrame);

        Mat inputROI = new Mat(this.mat, pupilRect);
        Imgproc.GaussianBlur(inputROI, inputROI, new Size(0, 0), 1.8);

        Imgproc.cvtColor(inputROI, inputROI, Imgproc.COLOR_RGB2GRAY, 0);

        //Imgcodecs.imwrite("roi_b4Blobs.jpg", inputROI);

        MatOfKeyPoint blobPoints = new MatOfKeyPoint();
        blobDetector.detect(inputROI, blobPoints);

        KeyPoint []ks = blobPoints.toArray();

        if (ks.length == 0) {
            this.hasPupil = false;
            inputROI.release();
            blobPoints.release();
            return;
        }

        /*
        for (KeyPoint k : ks)
            System.out.println("Blob at " + k.pt.x + ", " + k.pt.y + " d= " + k.size);

        Mat outputImage = new Mat();
        Features2d.drawKeypoints(inputROI, blobPoints, outputImage, new Scalar(0, 0, 255));
        Imgproc.circle (
            outputImage,
            ks[0].pt,                       //Center of the circle
            (int)(ks[0].size / 2),          //Radius
            new Scalar(0, 255, 0),
            2
        );

        Imgcodecs.imwrite("roi_blobs.jpg", outputImage);
        outputImage.release();
        */

        int pupilIndex = 0;
        if (ks.length > 1) {     // Look for the darkest circle on average across centre
            int minColorC = 255;

            for (int index = 0 ; index < ks.length ; index++) {
                int x = (int)ks[index].pt.x;
                int y = (int)ks[index].pt.y;
                int r = (int)ks[index].size / 2;

                int centerColor = getColorValue(inputROI, y, x - r, x + r);

                if (centerColor <= COLOR_UPPER_LIMIT && centerColor < minColorC) {
                    minColorC = centerColor;
                    pupilIndex = index;   
                }
            }
        }

        double xAdj = pupilRect.x;
        double yAdj = pupilRect.y;
        this.pupilX = (ks[pupilIndex].pt.x + xAdj - EYE_IMAGE_WIDTH / 2.0) * 1.23;  // TODO need to allow for off centre start
        this.pupilY = (ks[pupilIndex].pt.y + yAdj - EYE_IMAGE_HEIGHT / 2.0) * 1.23;  // TODO need to allow for off centre start
        this.pupilDiameter = ks[pupilIndex].size * 14.0 / 176.0;
        this.hasPupil = true;

        inputROI.release();
        blobPoints.release();
        return;
    }

    /** 
     * Get the average value of the pixel in the range of `from` to `to` along row `y` in the Mat `srcMat`.
     * @param srcMat The Mat from which to read the pixel values
     * @param y The row along which to read the pixel values
     * @param from The start column from which to read the pixel values
     * @param to The end column to which to read the pixel values
     * @return The average pixel value in the range of `from` to `to` along row `y` in the Mat `srcMat`. If we go out of image, return -1.
     */
    private int getColorValue(Mat srcMat, int y, int from, int to) {
        //if (srcMat.type() != CvType.CV_8UC1)
        //    throw new IllegalArgumentException("getColorValue: srcMat must be of type CV_8UC1");

        int pixelvalue = 0;
        try {
            for (int i = from; i < to; i++)
                pixelvalue += srcMat.get(y, i)[0];
            pixelvalue = pixelvalue / (to - from);
        } catch (Exception e) {
            return -1;
        }

        return pixelvalue;
    }
}