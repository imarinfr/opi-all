package org.lei.opi.core;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import org.opencv.core.*;
//import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;


/*
 * Contains pupil detection code based on Csharp code from Mitsuko Yoshida 16 March 2023.
 * (Quite a few modifications to try and avoid garbage collection...)
 */

public class CameraStreamerImo extends CameraStreamer {
    
    private static final int EYE_IMAGE_HEIGHT = 480;
    private static final int EYE_IMAGE_WIDTH = 640;

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

    public CameraStreamerImo(int port, int []deviceNumber) throws IOException {
        super(port, deviceNumber);
        pupilInfo = new PupilInfo(EYE_IMAGE_WIDTH, EYE_IMAGE_HEIGHT);
        //Imgproc.circle(circleMask, new Point(EYE_IMAGE_WIDTH / 2, EYE_IMAGE_HEIGHT / 2), MASK_RADIUS, WHITE, -1);
    }

    /**
     * Fill bytes with the image on socket. Assumes it has been written with writeBytes
     * @param socket An open socket from which to read
     * @return Device number read. -1 for error
     */
    public int readBytes(Socket socket) {
        int deviceNum = -1;
        try {
            deviceNum = (int)socket.getInputStream().read();
            if (deviceNum < 0)
                return -1;

            int n1 = (int)socket.getInputStream().read();
            int n2 = (int)socket.getInputStream().read();
            int n3 = (int)socket.getInputStream().read();
            int n4 = (int)socket.getInputStream().read();
            int n = (n1 << 24) | (n2 << 16) | (n3 << 8) | n4;

            bytesLock.lock();

            if (bytes.length != n)
                bytes = new byte[n];

            int off = 0; // current start of buffer (offset)
            while (off < n) {
                int readN = socket.getInputStream().read(bytes, off, n - off);
                off += readN;
            }
        } catch(IOException e) {
            System.out.println("readImageToBytes: trouble reading socket");
            e.printStackTrace();
        } finally {
            bytesLock.unlock();
        }
        return deviceNum;
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
    public void writeBytes(Socket socket, int deviceNumber) throws IOException, ConcurrentModificationException {
//System.out.println("Putting " + n + " bytes from camera " + i + " on socket.");
        if (!bytesLock.isLocked())
            throw new ConcurrentModificationException("Reading CameraStreamImo.bytes without locking it.");
        int n = bytes.length;
        socket.getOutputStream().write(deviceNumber);
        socket.getOutputStream().write(n >> 24);
        socket.getOutputStream().write((n >> 16) & 0xFF);
        socket.getOutputStream().write((n >>  8) & 0xFF);
        socket.getOutputStream().write( n        & 0xFF);
        socket.getOutputStream().write(bytes);
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
      * All of the code below is a translation of Csharp code from Mitsuko Yoshida 16 March 2023.
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
     * @return The average pixel value in the range of `from` to `to` along row `y` in the Mat `srcMat`
     */
    private int getColorValue(Mat srcMat, int y, int from, int to) {
        //if (srcMat.type() != CvType.CV_8UC1)
        //    throw new IllegalArgumentException("getColorValue: srcMat must be of type CV_8UC1");

        int pixelvalue = 0;
        for (int i = from; i < to; i++)
            pixelvalue += srcMat.get(y, i)[0];
        pixelvalue = pixelvalue / (to - from);

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
            
            // release memory
    }

    /* 
    private static final Scalar BLUE = new Scalar(0, 0, 255);

        // The Median is used over 5 items in a list to avoid mislead
    private MedianList centerXMedian  = new MedianList(5);
    private MedianList centerYMedian  = new MedianList(5);
    private MedianList pupilDiameterMedian = new MedianList(5);
    private MedianList brightCenterXMedian = new MedianList(5);
    private MedianList brightCenterYMedian = new MedianList(5);

    // ------ gaze tracking values
    private int frameCount = -4; // Throw away 4 frames when pupil is not detected (for blinking)
    private boolean isMeasureStart = false;
    private int preValidFrameCount = 0;
    private int validFrameCount = 0;
    private boolean MeasureStart = false;

        // Output values
    private double gaze_X_deg = 0.0;
    private double gaze_Y_deg = 0.0;
    private double pupilWidthHeight = 0.0;
        // Calibration values
    private boolean m_IsValidInitialOffset = false;
    private Point ptStOffset = new Point(0, 0);
    private double ptStOffsetX = 0.0;
    private double ptStOffsetY = 0.0;
    private int offsetListPos = 0;
    private int offsetListN = 15;
    private int listN = 40;
    private ArrayList<Double> offsetListX = new ArrayList<Double>(listN);
    private ArrayList<Double> offsetListY = new ArrayList<Double>(listN);
    private ArrayList<Double> centerListX = new ArrayList<Double>(listN);
    private ArrayList<Double> centerListY = new ArrayList<Double>(listN);
    private ArrayList<Double> brightCenterListX = new ArrayList<Double>(listN);
    private ArrayList<Double> brightCenterListY = new ArrayList<Double>(listN);
    private ArrayList<Double> initOffsetListX = new ArrayList<Double>(listN);
    private ArrayList<Double> initOffsetListY = new ArrayList<Double>(listN);
    */


    /**
     * Find pupil (x, y) and diameter
     * @param frame
     * @return
    public void processFrameWithTracking(Mat inputFrame) {
        Mat srcFrame = new Mat(inputFrame.size(), inputFrame.type());
        Mat outFrame = new Mat(inputFrame.size(), CvType.CV_8UC1);

        int brightPointThreshold = 230;

            // Create a circular mask for rough trimming
        int CenterX = inputFrame.width() / 2;
        int CenterY = inputFrame.height() / 2;

            // Create copies of the raw frames for each process
        inputFrame.copyTo(srcFrame, circleMask);
        inputFrame.copyTo(outFrame);

        Imgproc.GaussianBlur(srcFrame, srcFrame, new Size(0, 0), 1.8);

        Mat tempFrame = new Mat();
        srcFrame.copyTo(tempFrame);

            // Set ROI with the previous pupilRect size
            // If first time, or not detected in the previous frame, set ROI to the whole frame
        Mat ImageROI = new Mat(srcFrame, pupilRect);
        Size ImageROISize = ImageROI.size();

            // Detection of the pupil
        detectPupil(

            // reset ROIs   aht - WHY?
        srcFrame = new Mat();
        tempFrame.copyTo(srcFrame);

        if (pupilInfo.valid) {
                // detect the light spots (x4)
            pupilInfo.centerX = (int)centerXMedian.getMedian(pupilInfo.centerX);
            pupilInfo.centerY = (int)centerYMedian.getMedian(pupilInfo.centerY);
            pupilInfo.diameter = (int)pupilDiameterMedian.getMedian(pupilInfo.diameter);

            int r = (int)(pupilInfo.diameter * 176.0 / 14.0 / 2.0);

                // Create a new copy of the raw frame
            srcFrame = new Mat();
            tempFrame.copyTo(srcFrame);

                // Create new ROI with new pupilRect.
            srcFrame = new Mat(srcFrame, pupilRect);
            var srcSize = srcFrame.size();

            // Use MEDIAN smoothing (to avoid a too smooth edge due to gaussian blur)
            Imgproc.medianBlur(srcFrame, srcFrame, 5);
            // Binarize the image to get the bright spots
            Imgproc.threshold(srcFrame, srcFrame, brightPointThreshold, 255, Imgproc.THRESH_BINARY_INV);

                // Find the bright spots
            ArrayList<Point> brightPoints = new ArrayList<Point>();
            detectBrightPoint(srcFrame, brightPoints, pupilRect);

                // reset ROIs
            inputFrame.copyTo(srcFrame);

                // TODO this needs tidying up as pass by ref probably wont work or is memory wasteful
            Point topRight = new Point();
            Point topLeft = new Point();
            Point bottomRight = new Point();
            Point bottomLeft = new Point();
                // Select the 4 bright spots in the detected ones
            selectBrightPointFour(brightPoints, 
                new Point(pupilInfo.centerX, pupilInfo.centerY), 
                topRight, topLeft, bottomRight, bottomLeft, pupilRect);

            // If the bright spots are detected, use the 2 bottom ones to detect the center of the 4 (because same distance between the spots)
            boolean existBrightPoint = true;
            if ((bottomRight.x == 0.0 && bottomRight.y == 0.0) || (bottomLeft.x == 0.0 && bottomLeft.y == 0.0))
                existBrightPoint = false;

            // gaze tracking starts
            if (existBrightPoint) {
                double brightPointDistance = Math.sqrt((bottomLeft.x - bottomRight.x) * (bottomLeft.x - bottomRight.x) + (bottomLeft.y - bottomRight.y) * (bottomLeft.y - bottomRight.y));
                Point brightPointCenter = new Point((bottomLeft.x + bottomRight.x) / 2, (bottomLeft.y + bottomRight.y) / 2 - brightPointDistance / 2);
                brightPointCenter.x = brightCenterXMedian.getMedian(brightPointCenter.x + pupilRect.tl().x);
                brightPointCenter.y = brightCenterYMedian.getMedian(brightPointCenter.y + pupilRect.tl().y);

                //Adjust lens deviation
                brightPointCenter.x = 1.1 * brightPointCenter.x - 0.1 * 640 / 2;
                brightPointCenter.y = 1.1 * brightPointCenter.y - 0.1 * 480 / 2;
                Imgproc.circle(outFrame, brightPointCenter, 5, new Scalar(200, 200, 200), 1); // 輝点中心
                Imgproc.putText(outFrame, String.format("Bright Points Center (pxl): (%0.2f, %0.2f)", brightPointCenter.x, brightPointCenter.y), 
                    new Point(150, 400), Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, BLUE);
                Imgproc.putText(outFrame, String.format("PupilCenter (pxl): (%0.2f, %0.2f)", 
                    pupilInfo.centerX, pupilInfo.centerY), 
                    new Point(150, 420), Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, BLUE);

                if (m_IsValidInitialOffset) {
                    gaze_X_deg = -((brightPointCenter.x - pupilInfo.centerX - ptStOffsetX) * 1.230);
                    gaze_Y_deg = ((brightPointCenter.y - pupilInfo.centerY - ptStOffsetY) * 1.230);
                    Imgproc.putText(outFrame, String.format("Gaze (deg): (%0.2f, %0.2f)", gaze_X_deg, gaze_Y_deg), 
                        new Point(150, 440), Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, BLUE);
                }

                offsetListX.set(offsetListPos,  brightPointCenter.x - pupilInfo.centerX);
                offsetListY.set(offsetListPos,  brightPointCenter.y - pupilInfo.centerY);
                offsetListPos = (offsetListPos + 1) % offsetListN;

                if (MeasureStart == true)
                {
                    isMeasureStart = true;
                    m_IsValidInitialOffset = false;
                    MeasureStart = false;
                }
                if (!m_IsValidInitialOffset && isMeasureStart)
                {
                    if (frameCount - preValidFrameCount == 1)
                    {
                        int n = validFrameCount % listN;
                        centerListX.set(n, (double)pupilInfo.centerX);
                        centerListY.set(n, (double)pupilInfo.centerY);
                        brightCenterListX.set(n, brightPointCenter.x);
                        brightCenterListY.set(n, brightPointCenter.y);
                        validFrameCount++;

                        if (validFrameCount >= listN) {
                            for (int i = 0; i < listN; i++)
                            {
                                initOffsetListX.set(i, brightCenterListX.get(i) - centerListX.get(i));
                                initOffsetListY.set(i, brightCenterListY.get(i)- centerListY.get(i));
                            }
                            double averageInitOffsetX = initOffsetListX.stream().mapToDouble(a -> a).average().getAsDouble();
                            double averageInitOffsetY = initOffsetListY.stream().mapToDouble(a -> a).average().getAsDouble();

                            double varianceInitOffsetX = 0.0;
                            double varianceInitOffsetY = 0.0;
                            for (int i = 0; i < listN; i++) {
                                varianceInitOffsetX += (initOffsetListX.get(i) - averageInitOffsetX) * (initOffsetListX.get(i) - averageInitOffsetX);
                                varianceInitOffsetY += (initOffsetListY.get(i) - averageInitOffsetY) * (initOffsetListY.get(i) - averageInitOffsetY);
                            }
                            varianceInitOffsetX /= listN;
                            varianceInitOffsetY /= listN;

                            if (varianceInitOffsetX + varianceInitOffsetY < 0.64) {
                                ptStOffsetX = averageInitOffsetX;
                                ptStOffsetY = averageInitOffsetY;

                                m_IsValidInitialOffset = true;
                            }
                        }
                        if (validFrameCount >= 100)
                            isMeasureStart = false;
                    } else {
                        validFrameCount = 0;
                    }
                    preValidFrameCount = frameCount;
                }
            } else {
                pupilWidthHeight = 0.0;
                gaze_X_deg = 0;
                gaze_Y_deg = 0;
                pupilRect = new Rect(0, 0, inputFrame.width(), inputFrame.height());
            }

            pupilWidthHeight = (int)pupilInfo.diameter;
        } else {
            pupilWidthHeight = 0.0;
            gaze_X_deg = 0;
            gaze_Y_deg = 0;
            pupilRect = new Rect(0, 0, inputFrame.width(), inputFrame.height());
        }
        return;
    }

    private void detectBrightPoint(Mat srcMat, ArrayList<Point> points, Rect ROIRect) {
        Point pt;
        Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_RGB2GRAY);

        //use findcontour to find all the islands
        ArrayList<MatOfPoint> mContour = new ArrayList<MatOfPoint>();
        Mat mHierarchy = new Mat();
        Imgproc.findContours(srcMat, mContour, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        if (mContour.size() > 0) {
            for (int i = 0; i < mContour.size(); ++i) {
                Moments moment = Imgproc.moments(mContour.get(i));
                if (moment.m00 < 5) continue;
                if (moment.m00 > 150) continue;
                pt = new Point(moment.m10 / moment.m00, moment.m01 / moment.m00); 

                points.add(pt);
            }
        }
        //release memory
        return;
    }

    private void selectBrightPointFour(ArrayList<Point> brightPoints, Point center, Point topRight, Point topLeft, Point bottomRight, Point bottomLeft, Rect ROI) {
        ArrayList<Point> TR = new ArrayList<Point>();
        ArrayList<Point> TL = new ArrayList<Point>();
        ArrayList<Point> BR = new ArrayList<Point>();
        ArrayList<Point> BL = new ArrayList<Point>();

        for (Point point : brightPoints) {
            if (point.x + ROI.tl().x < center.x && point.y + ROI.tl().y < center.y) {
                TL.add(point);
            } else if (point.x + ROI.tl().x >= center.x && point.y + ROI.tl().y < center.y) {
                TR.add(point);
            } else if (point.x + ROI.tl().x < center.x && point.y + ROI.tl().y >= center.y) {
                BL.add(point);
            } else {
                BR.add(point);
            }
            topLeft = selectBrightPointOne(TL, center);
            topRight = selectBrightPointOne(TR, center);
            bottomRight = selectBrightPointOne(BR, center);
            bottomLeft = selectBrightPointOne(BL, center);
        }
    }

    private Point selectBrightPointOne(ArrayList<Point> targetPoints, Point center) {
        Point result = new Point(0, 0);
        if (targetPoints.size() > 1) {
            double distanceMin = 1000000;
            for (Point point : targetPoints) {
                double distance = (double)((center.x - point.x) * (center.x - point.x) + (center.y - point.y) * (center.y - point.y));
                if (distance < distanceMin) {
                    distanceMin = distance;
                    result = point;
                }
            }
        } else if (targetPoints.size() == 1) {
            result = targetPoints.get(0);
        }
        return result;
    }

     */

}