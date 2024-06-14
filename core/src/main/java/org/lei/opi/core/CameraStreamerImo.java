package org.lei.opi.core;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;

import org.opencv.core.*;
import org.opencv.imgproc.*;



public class CameraStreamerImo extends CameraStreamer {
    
    private static final Scalar WHITE = new Scalar(1, 1, 1);
    private static final Scalar BLUE = new Scalar(0, 0, 1);

    /** ROI for pupil detection. Updated from last frame as we go, starts with whole image */
    private Rect pupilRect = new Rect(0, 0, 640, 480); 

    private Rect newROIRect;

        // The Median is used over 5 items in a list to avoid mislead
    private MedianList pupilCenterXMedian  = new MedianList(5);
    private MedianList pupilCenterYMedian  = new MedianList(5);
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
    private ArrayList<Double> pupilCenterListX = new ArrayList<Double>(listN);
    private ArrayList<Double> pupilCenterListY = new ArrayList<Double>(listN);
    private ArrayList<Double> brightCenterListX = new ArrayList<Double>(listN);
    private ArrayList<Double> brightCenterListY = new ArrayList<Double>(listN);
    private ArrayList<Double> initOffsetListX = new ArrayList<Double>(listN);
    private ArrayList<Double> initOffsetListY = new ArrayList<Double>(listN);

    /** Weak instance just to allow calling of readBytes. Why not make it static? */
    public CameraStreamerImo() { ; }

    public CameraStreamerImo(int port, int []deviceNumber) throws IOException {
        super(port, deviceNumber);
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
            throw new ConcurrentModificationException("Reading CamerStreamImo.bytes without locking it.");
        int n = bytes.length;
        socket.getOutputStream().write(deviceNumber);
        socket.getOutputStream().write(n >> 24);
        socket.getOutputStream().write((n >> 16) & 0xFF);
        socket.getOutputStream().write((n >>  8) & 0xFF);
        socket.getOutputStream().write( n        & 0xFF);
        socket.getOutputStream().write(bytes);
    }

    /**
     * Find pupil (x, y) and diameter
     * Translation of Csharp code from Mitsuko Yoshida 16 March 2023.
     * @param frame
     * @return
     */
    protected boolean getImageValues(Mat inputFrame) {
        Mat srcFrame = new Mat(inputFrame.size(), inputFrame.type());
        Mat outFrame = new Mat(inputFrame.size(), CvType.CV_8UC1);

            // Create a circular mask for rough trimming
        int CenterX = inputFrame.width() / 2;
        int CenterY = inputFrame.height() / 2;

        Mat blackMask = new Mat(inputFrame.size(), inputFrame.type());
        Imgproc.circle(blackMask, new Point(CenterX, CenterY), 125, WHITE);

            // Create copies of the raw frames for each process
        inputFrame.copyTo(srcFrame, blackMask);
        inputFrame.copyTo(outFrame);

        Imgproc.GaussianBlur(srcFrame, srcFrame, new Size(0, 0), 1.8);

        Mat tempFrame = new Mat();
        srcFrame.copyTo(tempFrame);

            // Set ROI with the previous pupilRect size
            // If first time, or not detected in the previous frame, set ROI to the whole frame
        var ImageROI = new Mat(srcFrame, pupilRect);

        double pupilDiameter = 0.0;
        Point pupilCenter = new Point(0, 0);
        RotatedRect ellipse = new RotatedRect();

            // Detection of the pupil
        boolean existpupil = detectPupil(ImageROI, outFrame, pupilDiameter, pupilCenter, ellipse, pupilRect, tempFrame);
    
        if (existpupil) {
            processingResults.put("x", (int)pupilCenter.x);
            processingResults.put("y", (int)pupilCenter.y);
            processingResults.put("d", (int)pupilDiameter);
        } 

        return existpupil;
    }

     /* ----------------------------------------------------------------------
      * All of the code below is a translation of Csharp code from Mitsuko Yoshida 16 March 2023.
      * ----------------------------------------------------------------------
      */


    /**
     * Find pupil (x, y) and diameter
     * @param frame
     * @return
     */
    public void processFrameWithTracking(Mat inputFrame) {
        Mat srcFrame = new Mat(inputFrame.size(), inputFrame.type());
        Mat outFrame = new Mat(inputFrame.size(), CvType.CV_8UC1);

        int brightPointThreshold = 230;

            // Create a circular mask for rough trimming
        int CenterX = inputFrame.width() / 2;
        int CenterY = inputFrame.height() / 2;

        Mat blackMask = new Mat(inputFrame.size(), inputFrame.type());
        Imgproc.circle(blackMask, new Point(CenterX, CenterY), 125, WHITE);

            // Create copies of the raw frames for each process
        inputFrame.copyTo(srcFrame, blackMask);
        inputFrame.copyTo(outFrame);

        Imgproc.GaussianBlur(srcFrame, srcFrame, new Size(0, 0), 1.8);

        Mat tempFrame = new Mat();
        srcFrame.copyTo(tempFrame);

            // Set ROI with the previous pupilRect size
            // If first time, or not detected in the previous frame, set ROI to the whole frame
        var ImageROI = new Mat(srcFrame, pupilRect);
        var ImageROISize = ImageROI.size();

        double pupilDiameter = 0.0;
        Point pupilCenter = new Point(0, 0);
        RotatedRect ellipse = new RotatedRect();

            // Detection of the pupil
        boolean existpupil = detectPupil(ImageROI, outFrame, pupilDiameter, pupilCenter, ellipse, pupilRect, tempFrame);

            // reset ROIs   aht - WHY?
        srcFrame = new Mat();
        tempFrame.copyTo(srcFrame);

        if (existpupil) {
                // detect the light spots (x4)
            pupilCenter.x = pupilCenterXMedian.getMedian(pupilCenter.x);
            pupilCenter.y = pupilCenterYMedian.getMedian(pupilCenter.y);
            pupilDiameter = pupilDiameterMedian.getMedian(pupilDiameter);

            int r = (int)(pupilDiameter * 176.0 / 14.0 / 2.0);

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


            Point topRight = new Point();
            Point topLeft = new Point();
            Point bottomRight = new Point();
            Point bottomLeft = new Point();
                // Select the 4 bright spots in the detected ones
            selectBrightPointFour(brightPoints, pupilCenter, topRight, topLeft, bottomRight, bottomLeft, pupilRect);

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
                Imgproc.putText(outFrame, String.format("PupilCenter (pxl): (%0.2f, %0.2f)", pupilCenter.x, pupilCenter.y), 
                    new Point(150, 420), Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, BLUE);

                if (m_IsValidInitialOffset) {
                    gaze_X_deg = -((brightPointCenter.x - pupilCenter.x - ptStOffsetX) * 1.230);
                    gaze_Y_deg = ((brightPointCenter.y - pupilCenter.y - ptStOffsetY) * 1.230);
                    Imgproc.putText(outFrame, String.format("Gaze (deg): (%0.2f, %0.2f)", gaze_X_deg, gaze_Y_deg), 
                        new Point(150, 440), Imgproc.FONT_HERSHEY_SIMPLEX, 0.7, BLUE);
                }

                offsetListX.set(offsetListPos,  brightPointCenter.x - pupilCenter.x);
                offsetListY.set(offsetListPos,  brightPointCenter.y - pupilCenter.y);
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
                        pupilCenterListX.set(n, pupilCenter.x);
                        pupilCenterListY.set(n, pupilCenter.y);
                        brightCenterListX.set(n, brightPointCenter.x);
                        brightCenterListY.set(n, brightPointCenter.y);
                        validFrameCount++;

                        if (validFrameCount >= listN) {
                            for (int i = 0; i < listN; i++)
                            {
                                initOffsetListX.set(i, brightCenterListX.get(i) - pupilCenterListX.get(i));
                                initOffsetListY.set(i, brightCenterListY.get(i)- pupilCenterListY.get(i));
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

            pupilWidthHeight = (int)pupilDiameter;
        } else {
            pupilWidthHeight = 0.0;
            gaze_X_deg = 0;
            gaze_Y_deg = 0;
            pupilRect = new Rect(0, 0, inputFrame.width(), inputFrame.height());
        }
        return;
    }

    /**
     * 
     * @param InputROI
     * @param dstMat  Output Mat
     * @param pupilDiameter
     * @param pupilCenter
     * @param ellipse
     * @param pupilRect
     * @param rawFrame
     * @return True if found a pupil, false otherwise
     */
    private boolean detectPupil(Mat InputROI, Mat dstMat, double pupilDiameter, 
                                Point pupilCenter, RotatedRect ellipse, Rect pupilRect, Mat rawFrame) {

        // Rough detection of the pupil
        int pupilColorUpperLimit = 80;
        int houghCirclesThreshold = 80;

        boolean result = false;
        //Mat tempMatforReset;
        //InputROI.copyTo(tempMatforReset);

        Mat circles = new Mat();   // TODO would MatOfPoint3 or Mat.Tuple3<double> be better?
            // Houghcircles does not need to be binarized yet. "4" is the grid for accuracy (the smaller the more accurate, but the more misdetections)
        Imgproc.cvtColor(InputROI, InputROI, Imgproc.COLOR_RGB2GRAY, 0);
        Imgproc.HoughCircles(InputROI, circles, Imgproc.HOUGH_GRADIENT, 2, 500, houghCirclesThreshold, 40, 15, 30);

        if (circles.type() > 0) {
            System.out.print("\n\tFound some circles: " + circles.size() + " " + circles.type());
            for (int r = 0 ; r < circles.rows() ; r++) {
                double [] v = circles.get(r, 0);
                System.out.println("\n\t\t" + v.length + " values: " + Arrays.toString(v));
            }
        }

            // if no circles detected, stop and reset pupilRect to srcMat size.
        if (circles.type() == 0) {
            pupilDiameter = 0;
            pupilRect = new Rect(0, 0, rawFrame.width(), rawFrame.height());
            return false;
        }

            // Create a list of all the circles detected, and change the coordinates to the absolute frame
            // Then, check if next to the detected circle, the colors are as expected:
            // If the detected circle is a pupil, the color on its left en right should be lower than the center of the circle, 
            // if not, it is probably not the pupil. White -> Black -> White
            // If few circles meet the expectations, then check if the black is really black.
        int pupilColorC = 255; // Center
        int pupilColorR = 255; // Right
        int pupilColorL = 255; // Left
        ArrayList<double []> pupil = new ArrayList<double[]>();
        for (int r = 0 ; r < circles.rows() ; r++) {
            int x = (int)circles.get(r, 0)[0];
            int y = (int)circles.get(r, 0)[1];
            int rr = (int)circles.get(r, 0)[2];
            if (InputROI.width() == 120) {
                x = x - (int)pupilRect.tl().x;
                y = y - (int)pupilRect.tl().y;
            }
            rr = rr > 25 ? 25 : rr;
            int centerColor = getColorValue(InputROI, y, x - rr, x + rr);
            int leftsideColor = getColorValue(InputROI, y, x - 2 * rr - 2, x - 2 * rr + 3);
            int rightsideColor = getColorValue(InputROI, y, x + 2 * rr - 2, x + 2 * rr + 3);
            int mincolor = rightsideColor < leftsideColor ? rightsideColor : leftsideColor;
            double threshold1 = (centerColor + mincolor) / 2.0;

            if (centerColor <= pupilColorUpperLimit) {
                pupil.add(circles.get(r, 0));

                if (pupilColorC > centerColor) {
                    pupilColorC = centerColor;
                    pupilColorR = rightsideColor;
                    pupilColorL = leftsideColor;
                }
            }
        }

        if (pupil.size() > 0) {
            int edge = 60;
            newROIRect = new Rect((int)(pupil.get(0)[0] - edge), (int)(pupil.get(0)[1] - edge), 2 * edge, 2 * edge);

            if (!(0 <= newROIRect.x + newROIRect.width && newROIRect.x <= rawFrame.width() && 
                  0 <= newROIRect.y + newROIRect.height && newROIRect.y <= rawFrame.height())) {
                pupilDiameter = 0;
                pupilRect = new Rect(0, 0, rawFrame.width(), rawFrame.height());

                return false;
            }
                // reset roi
            rawFrame.copyTo(InputROI);

                // set new roi
            InputROI = new Mat(InputROI, newROIRect);

            Point gravityCenter = new Point();
            Point pupilSize = new Point();

                // Calculate the minimum between pupil color right and left.
                // If (pupilColoreR < pupilColoreL == true) -> pupilColoreR. Else{ pupilColoreL}
                // then, set the threshold as the mean value between center and minimum
            int pupilColorMin = pupilColorR < pupilColorL ? pupilColorR : pupilColorL;

            double threshold = (pupilColorC + pupilColorMin) / 2.0;

                //binarization
            Imgproc.threshold(InputROI, InputROI, threshold, 255, Imgproc.THRESH_BINARY_INV);

                // Get all the input informations with getPupilInfo. (use FindContour)
            Imgproc.cvtColor(InputROI, InputROI, Imgproc.COLOR_RGB2GRAY);
            result = getPupilInfo(InputROI, dstMat, gravityCenter, pupilCenter, pupilSize, ellipse, newROIRect);
            if (result) {
                pupilDiameter = pupilSize.y * 14.0 / 176.0;
                pupilRect = new Rect((int)(pupilCenter.x - edge), (int)(pupilCenter.y - edge), 2 * edge, 2 * edge);
                if (!(0 <= pupilRect.x + pupilRect.width && pupilRect.x <= rawFrame.width() && 0 <= pupilRect.y + pupilRect.height && pupilRect.y <= rawFrame.height())) {
                    pupilDiameter = 0;
                    pupilRect = new Rect(0, 0, rawFrame.width(), rawFrame.height());
                    return false;
                }
            }
        }
        return result;
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

    private boolean getPupilInfo(Mat InputROI, Mat dst, Point gravityCenter, Point pupilCenter, Point pupilSize, RotatedRect ellipse, Rect srcROI) {
        boolean result = false;
        Point pt;

        ArrayList<MatOfPoint> mContour = new ArrayList<MatOfPoint>();
        Mat mHierarchy = new Mat();

        double ellipseCircleLevelMax = 0;
            // use findcontour to find all the islands
        Imgproc.findContours(InputROI, mContour, mHierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int i = 0; i < mContour.size(); ++i) {
            //if (mContour.Length <= 5) continue;
            Moments moment = Imgproc.moments(mContour.get(i));
            if (moment.m00 == 0) continue;
            pt = new Point(moment.m10 / moment.m00, moment.m01 / moment.m00);

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
                ellipse = Imgproc.fitEllipse(m2f);
                if (!(ellipse.center.x >= 0 && ellipse.center.y >= 0 && ellipse.center.x < InputROI.width() && ellipse.center.y < InputROI.height())) // ROI width and height
                    continue;

                if (ellipse.size.width > 120 || ellipse.size.height > 120)
                    continue;

                double ellipseCircleLevel = ellipse.size.width > ellipse.size.height ? ellipse.size.height / ellipse.size.width : ellipse.size.width / ellipse.size.height;

                if (ellipseCircleLevel > ellipseCircleLevelMax) {
                    gravityCenter = new Point(pt.x + srcROI.tl().x, pt.y + srcROI.tl().y);
                    pupilCenter = new Point(ellipse.center.x + srcROI.tl().x, ellipse.center.y + srcROI.tl().y);
                    pupilSize = new Point(ellipse.size.width, ellipse.size.height);
                    ellipseCircleLevelMax = ellipseCircleLevel;
                    result = true;
                    ellipse.center.x = ellipse.center.x + srcROI.tl().x;
                    ellipse.center.y = ellipse.center.y + srcROI.tl().y;
                    Imgproc.ellipse(dst, ellipse, WHITE);
                }
            }
        }
            
            // release memory

        return result;
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

    private void selectBrightPointFour(ArrayList<Point> brightPoints, Point pupilCenter, Point topRight, Point topLeft, Point bottomRight, Point bottomLeft, Rect ROI) {
        ArrayList<Point> TR = new ArrayList<Point>();
        ArrayList<Point> TL = new ArrayList<Point>();
        ArrayList<Point> BR = new ArrayList<Point>();
        ArrayList<Point> BL = new ArrayList<Point>();

        for (Point point : brightPoints) {
            if (point.x + ROI.tl().x < pupilCenter.x && point.y + ROI.tl().y < pupilCenter.y) {
                TL.add(point);
            } else if (point.x + ROI.tl().x >= pupilCenter.x && point.y + ROI.tl().y < pupilCenter.y) {
                TR.add(point);
            } else if (point.x + ROI.tl().x < pupilCenter.x && point.y + ROI.tl().y >= pupilCenter.y) {
                BL.add(point);
            } else {
                BR.add(point);
            }
            topLeft = selectBrightPointOne(TL, pupilCenter);
            topRight = selectBrightPointOne(TR, pupilCenter);
            bottomRight = selectBrightPointOne(BR, pupilCenter);
            bottomLeft = selectBrightPointOne(BL, pupilCenter);
        }
    }

    private Point selectBrightPointOne(ArrayList<Point> targetPoints, Point pupilCenter) {
        Point result = new Point(0, 0);
        if (targetPoints.size() > 1) {
            double distanceMin = 1000000;
            for (Point point : targetPoints) {
                double distance = (double)((pupilCenter.x - point.x) * (pupilCenter.x - point.x) + (pupilCenter.y - point.y) * (pupilCenter.y - point.y));
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

}