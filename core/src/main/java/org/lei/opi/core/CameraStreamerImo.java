package org.lei.opi.core;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import org.lei.opi.core.definitions.FrameInfo;
import org.opencv.core.*;
//import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;

import es.optocom.jovp.definitions.ViewEye;


/*
 * Contains pupil detection code based on Csharp code from Mitsuko Yoshida 16 March 2023.
 * (Quite a few modifications to try and avoid garbage collection...)
 */

public class CameraStreamerImo extends CameraStreamer {
    private static final int CODE_LEFT = 0;
    private static final int CODE_RIGHT = 1;
    private static final int EYE_IMAGE_HEIGHT = 480;
    private static final int EYE_IMAGE_WIDTH = 640;

    /** Working area for {@link writeBytes} */
    private byte []bytes = new byte[EYE_IMAGE_HEIGHT * EYE_IMAGE_WIDTH * 3];

    /** Circular region for cropping out in {@link detectPupil} */
    //private static final Scalar WHITE = new Scalar(255, 255, 255);
    //private final Mat circleMask = new Mat(EYE_IMAGE_HEIGHT, EYE_IMAGE_WIDTH, CvType.CV_8UC3, Scalar.all(0));
    private final int MASK_RADIUS = 125;

    /** Current window for looking for pupil. Might change size as the stream progresses. */
    //Rect pupilRect = new Rect(0, 0, EYE_IMAGE_WIDTH, EYE_IMAGE_HEIGHT);
    Rect pupilRect = new Rect(EYE_IMAGE_WIDTH / 2 - MASK_RADIUS, EYE_IMAGE_HEIGHT / 2 - MASK_RADIUS, 
                              MASK_RADIUS * 2, MASK_RADIUS * 2);

    /** Brightest a pupil can get for {@link detectPupil} */
    private final int COLOR_UPPER_LIMIT = 80;
    /** Some parameter for edge detection... for {@link detectPupil} */
    private final int HOUGH_CIRCLES_THRESHOLD = 80;
    /** Edge around roughly detected pupil to cut out for ellipse finding in {@link detectPupil} */
    final int PADDING = 60;   

    /** Working are for {@link detectPupil} */
    Rect newROIRect = new Rect(0, 0, 2 * PADDING, 2 * PADDING);


    /** Weak instance just to allow calling of readBytes. Why not make it static? */
    public CameraStreamerImo() { ; }

    public CameraStreamerImo(int port, int deviceNumberLeft, int deviceNumberRight) throws IOException {
        super(port, deviceNumberLeft, deviceNumberRight);
        pupilInfo = new PupilInfo(EYE_IMAGE_WIDTH, EYE_IMAGE_HEIGHT);
        //Imgproc.circle(circleMask, new Point(EYE_IMAGE_WIDTH / 2, EYE_IMAGE_HEIGHT / 2), MASK_RADIUS, WHITE, -1);
    }

    /**
     * Fill bytes with the image on socket. Assumes it has been written with writeBytes
     * @param socket An open socket from which to read
     * @return Eye read (NONE for error)
     */
    public ViewEye readBytes(Socket socket, byte []dst) throws IndexOutOfBoundsException {
        ViewEye eye = ViewEye.NONE;
        try {
            int eyeCode = (int)socket.getInputStream().read();
            if (eyeCode < 0)
                return eye;
            eye = eyeCode == CODE_LEFT ? ViewEye.LEFT : ViewEye.RIGHT;

            int n1 = (int)socket.getInputStream().read();
            int n2 = (int)socket.getInputStream().read();
            int n3 = (int)socket.getInputStream().read();
            int n4 = (int)socket.getInputStream().read();
            int n = (n1 << 24) | (n2 << 16) | (n3 << 8) | n4;

            if (dst.length != n)
                throw new IndexOutOfBoundsException("CameraStreamerImo readBytes needs a bigger destination.");

            int off = 0; // current start of buffer (offset)
            while (off < n) {
                int readN = socket.getInputStream().read(dst, off, n - off);
                off += readN;
            }
        } catch(IOException e) {
            System.out.println("CameraStreamerImo readBytes: trouble reading socket");
            e.printStackTrace();
        }
        return eye;
    }
    
    /**
     * Write static bytes array out on socket as 
     *       1 byte for device number
     *       4 bytes for length of data, n
     *       n bytes
     *
     * @param socket Open socket on which to write bytes
     * @param deviceNumber To write before bytes
     * @throws IOException
     * @throws ConcurrentModificationException You should CameraStreamerImo.bytesLock.lock() before calling this.
     */
    public void writeBytes(Socket socket, ViewEye eye, FrameInfo frame) {
        int n = frame.mat().channels() * frame.mat().rows() * frame.mat().cols();
        if (n != bytes.length)
            bytes = new byte[n];
        frame.mat().get(0, 0, this.bytes);

        try {
             socket.getOutputStream().write(eye == ViewEye.LEFT ? CODE_LEFT : CODE_RIGHT);
             socket.getOutputStream().write(n >> 24);
             socket.getOutputStream().write((n >> 16) & 0xFF);
             socket.getOutputStream().write((n >>  8) & 0xFF);
             socket.getOutputStream().write( n        & 0xFF);
             socket.getOutputStream().write(bytes);
        } catch (IOException e) {
            System.out.println("Error writing eye image bytes to socket");
            e.printStackTrace();
        }
    }

    /**
     * Process frame to find (x, y) and diameter of pupil and put the result in {@link pupilInfo}.
     * Translation of Csharp code from Mitsuko Yoshida 16 March 2023.
     * @param frame
     * @return
     */
    protected void getImageValues(Mat inputFrame) {
        detectPupil(inputFrame);
//Imgcodecs.imwrite("my.jpg", imageROI);
    }

     /* ----------------------------------------------------------------------
      * All of the code below is a translation of Csharp code supplied by Mitsuko Yoshida 16 March 2023.
      * ----------------------------------------------------------------------
      */


    /**
     * Look for a pupil ellipse in a central square region of {@link inputFrame} defined by {@ link pupilRect}.
     * Update {@link pupilInfo} as a side effect.
     *  1) Cut out pupilRect is bigger than mask radius
     *  2) Gaussian blur
     *  3) Crop out pupilRect
     *  4) Hough transform to find circles
     *  5) For each circle, check color to the left and right and find darkest that is white -> black -> white
     *
     * @param inputFrame An input image straight from camera. Should be EYE_IMAGE_WIDTH x EYE_IMAGE_HEIGHT
     */
    private void detectPupil(Mat inputFrame) {
        //System.out.println(" " + inputFrame.size());
        //Imgcodecs.imwrite("input.jpg", inputFrame);
        Mat inputROI = new Mat(inputFrame, pupilRect);
        Imgproc.GaussianBlur(inputROI, inputROI, new Size(0, 0), 1.8);

        Imgproc.cvtColor(inputROI, inputROI, Imgproc.COLOR_RGB2GRAY, 0);

        //Imgcodecs.imwrite("roi_b4Hough.jpg", inputROI);
            // Rough detection of the pupil
            // Houghcircles does not need to be binarized yet. "4" is the grid for accuracy (the smaller the more accurate, but the more misdetections)
        Mat circles = new Mat();   // TODO would MatOfPoint3 or Mat.Tuple3<double> be better?
        Imgproc.HoughCircles(inputROI, circles, Imgproc.HOUGH_GRADIENT, 2, 500, HOUGH_CIRCLES_THRESHOLD, 40, 15, 30);

        //if (circles.type() > 0) {
        //    System.out.print("\n\tFound some circles: " + circles.size() + " " + circles.type());
        //    for (int r = 0 ; r < circles.rows() ; r++) {
        //        double [] v = circles.get(r, 0);
        //        System.out.println("\n\t\t" + v.length + " values: " + Arrays.toString(v));
        //    }
        //}

            // if no circles detected, stop and reset pupilRect to full size.
        if (circles.type() == 0) {
            pupilInfo.reset();
            inputROI.release();
            circles.release();
            //srcFrame.release();
            return;
        }

            // Look for the darkest circle where the color on its left and right 
            // should be lower than the center of the circle, 
            // (If not, it is probably not the pupil. White -> Black -> White)
        int minColorC = 255; // Center
        int minColorR = 255; // Right
        int minColorL = 255; // Left
        int pupilIndex = -1;
        for (int index = 0 ; index < circles.rows() ; index++) {
            int x = (int)circles.get(index, 0)[0];
            int y = (int)circles.get(index, 0)[1];
            int r = Math.min((int)circles.get(index, 0)[2], 25);  // cap r at 25 (for some reason)

            int centerColor = getColorValue(inputROI, y, x - r, x + r);
            int leftColor = getColorValue(inputROI, y, x - 2 * r - 2, x - 2 * r + 3);
            int rightColor = getColorValue(inputROI, y, x + 2 * r - 2, x + 2 * r + 3);

            if (centerColor <= COLOR_UPPER_LIMIT && centerColor < minColorC) {
                minColorC = centerColor;
                minColorR = rightColor;
                minColorL = leftColor;
                if (pupilIndex == -1) pupilIndex = index;   // just keep the first one, for some reason...
            }
        }

            // Send a little square around the centre off to getPupilInfo to find the ellipse.
            // If the little square is outside pupilRect, give up
        if (pupilIndex > -1) {
            // Imgcodecs.imwrite("roi_b4tight.jpg", inputROI);
            if (pupilRect.width > 2 * PADDING) {
                double []c = circles.get(pupilIndex, 0);
                newROIRect.x = (int)(c[0] - PADDING);
                newROIRect.y = (int)(c[1] - PADDING);

                try {
                    inputROI = new Mat(inputROI, newROIRect);  
                } catch (Exception e) {
                        // We have strayed outside the pupilRect, reset everything and give up
                    pupilRect.x = EYE_IMAGE_WIDTH / 2 - MASK_RADIUS;
                    pupilRect.y = EYE_IMAGE_HEIGHT / 2 - MASK_RADIUS;
                    pupilRect.width = MASK_RADIUS * 2;
                    pupilRect.height = MASK_RADIUS * 2;
                    pupilInfo.reset();
                    inputROI.release();
                    circles.release();
                    //srcFrame.release();
                    return;
                }
            }
            // Imgcodecs.imwrite("roi_afttight.jpg", inputROI);

                // Set the threshold as the mean value between center and minimum
            int colorMin = Math.min(minColorR, minColorL);
            double threshold = (minColorC + colorMin) / 2.0;

                // Convert to binary image
            Imgproc.threshold(inputROI, inputROI, threshold, 255, Imgproc.THRESH_BINARY_INV);

                // Get all the input informations with getPupilInfo. (use FindContour)
            //Imgproc.cvtColor(inputROI, inputROI, Imgproc.COLOR_RGB2GRAY);  // No need for this? already GRAY
            getPupilInfo(inputROI, newROIRect);
        }
        inputROI.release();
        circles.release();
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

    /**
     * Get the pupil information from the inputROI and set pupilInfo appropriately.
     * +----------------------------------+
     * |                  inputFrame      |
     * |      +-------------------+       |
     * |      |   pupilRect       |       |
     * |      |    +------+       |       |
     * |      |    |      |       |       |
     * |      |    |srcROI|       |       |
     * |      |    |      |       |       |
     * |      |    +------+       |       |
     * |      +-------------------+       |
     * +----------------------------------+
     * @param inputROI The Mat in which to find contours and ellipse - binary image
     * @param srcROI The Rect from which the inputROI was cropped out of the whole frame
     */
    private void getPupilInfo(Mat inputROI, Rect srcROI) {
        //Imgcodecs.imwrite("gpi.jpg", inputROI);
        ArrayList<MatOfPoint> mContour = new ArrayList<MatOfPoint>();
        Mat mHierarchy = new Mat();

        double ellipseCircleLevelMax = 0;
            // use findcontour to find all the islands
        Imgproc.findContours(inputROI, mContour, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int i = 0; i < mContour.size(); ++i) {
            //if (mContour.Length <= 5) continue;
            Moments moment = Imgproc.moments(mContour.get(i));
            if (moment.m00 == 0) continue;

            double area;// = 0.0;
            double perimeter;// = 0.0;
            double circleLevel;// = 0.0;

            //面積 check the area in the contour, do not count if too small or too big
            area = moment.m00;
            if (area > 6000) continue;
            if (area < 100) continue;
            //周囲長

            MatOfPoint2f m2f = new MatOfPoint2f();
            mContour.get(i).convertTo(m2f, CvType.CV_32F);
            perimeter = Imgproc.arcLength(m2f, true);
            //円形度 
            if (perimeter > 0.0)
                circleLevel = 4.0 * Math.PI * area / (perimeter * perimeter); // 4*pi*area/(perimeter^2)
            else
                continue;

            double area_r = Math.sqrt(area / Math.PI);
            double perimeter_r = perimeter / (2 * Math.PI);
            // check the circleness of the detected island. if the circle is very flat, or not circular at all, then NG
            if (circleLevel > 0.6 && (area_r > 10 && perimeter_r > 10)) {
                // Check if ellipse fits inside the ROI 
                RotatedRect ellipse = Imgproc.fitEllipse(m2f);
                if (!(ellipse.center.x >= 0 && ellipse.center.y >= 0 && ellipse.center.x < inputROI.width() && ellipse.center.y < inputROI.height())) // ROI width and height
                    continue;

                if (ellipse.size.width > 120 || ellipse.size.height > 120)
                    continue;

                double ellipseCircleLevel = ellipse.size.width > ellipse.size.height ? ellipse.size.height / ellipse.size.width : ellipse.size.width / ellipse.size.height;

                if (ellipseCircleLevel > ellipseCircleLevelMax) {
                    double xAdj = srcROI.x + pupilRect.x;
                    double yAdj = srcROI.y + pupilRect.y;
                    pupilInfo.centerX = (ellipse.center.x + xAdj - EYE_IMAGE_WIDTH / 2.0) * 1.23;  // TODO need to allow for off centre start
                    pupilInfo.centerY = (ellipse.center.y + yAdj - EYE_IMAGE_HEIGHT / 2.0) * 1.23;  // TODO need to allow for off centre start
                    pupilInfo.bb_tl_x = (int)(ellipse.boundingRect().tl().x + xAdj);
                    pupilInfo.bb_tl_y = (int)(ellipse.boundingRect().tl().y + yAdj);
                    pupilInfo.bb_width = (int)ellipse.boundingRect().width;
                    pupilInfo.bb_height = (int)ellipse.boundingRect().height;
                    pupilInfo.diameter = ellipse.size.height * 14.0 / 176.0;
                    pupilInfo.valid = true;
                }
            }
        }
    }
}