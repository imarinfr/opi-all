package org.lei.opi.core;

import java.io.IOException;
import java.net.Socket;
import java.util.ConcurrentModificationException;

import org.opencv.core.*;
import org.opencv.imgproc.*;


public class CameraStreamerImo extends CameraStreamer {
    
    private class CircleSegment {
        public Point Center;
        public double Radius;
    }

    /** ROI for pupil detection. Updated from last frame as we go, starts with whole image */
    private Rect pupilRect = new Rect(0, 0, 640, 480); 

    /** Weak instance just to allow calling of readBytes. Why not make it static? */
    public CameraStreamerImo() { ; }

    public CameraStreamerImo(int port, int []deviceNumber) throws IOException {
        super(port, deviceNumber);
    }
    protected void getImageValues(Mat frame) {
        processingResults.put("x", 66);
        processingResults.put("y", 77);
        processingResults.put("d", 22);
        processFrame(frame);
    }

    /**
     * Find pupil (x, y) and diameter
     * @param frame
     * @return
     */
    public void processFrame(Mat inputFrame) {
        Mat srcFrame = new Mat(inputFrame.size(), inputFrame.type());
        Mat outFrame = new Mat(inputFrame.size(), CvType.CV_8UC1);

        int brightPointThreshold = 230;

            // Create a circular mask for rough trimming
        int CenterX = inputFrame.width() / 2;
        int CenterY = inputFrame.height() / 2;

        Mat blackMask = new Mat(inputFrame.size(), inputFrame.type());
        Imgproc.circle(blackMask, new Point(CenterX, CenterY), 125, new Scalar(1, 1, 1));

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
        boolean existpupil = DetectPupil(ImageROI, outFrame, pupilDiameter, pupilCenter, ellipse, pupilRect, tempFrame);

            // reset ROIs   WHY?
        srcFrame = new Mat();
        tempFrame.copyTo(srcFrame);

        return;

/* 

        if (existpupil) {
                // detect the light spots (x4)
            pupilCenter.X = pupilCenterXMedian.GetMedian(pupilCenter.X);
            pupilCenter.Y = pupilCenterYMedian.GetMedian(pupilCenter.Y);
            pupilDiameter = pupilDiameterMedian.GetMedian(pupilDiameter);

                        int r = (int)(pupilDiameter * 176.0 / 14.0 / 2.0);
                        //Cv2.Circle(outFrame, new Point((int)pupilCenter.X, (int)pupilCenter.Y), r, new Scalar(250, 250, 250));
                        // Create a new copy of the raw frame
                        srcFrame = new Mat();
                        Cv2.CopyTo(tempFrame, srcFrame);

                        //PupilRectlabel.Text = pupilRect.ToString();
                        // Create new ROI with new pupilRect.
                        srcFrame = new Mat(srcFrame, pupilRect);
                        var srcSize = srcFrame.Size();

                        // Use MEDIAN smoothing (to avoid a too smooth edge due to gaussian blur)
                        Cv2.MedianBlur(srcFrame, srcFrame, 5);
                        // Binarize the image to get the bright spots
                        Cv2.Threshold(srcFrame, srcFrame, brightPointThreshold, 255, ThresholdTypes.BinaryInv);

                        List<Point2d> brightPoints = new List<Point2d>();
                        // Find the bright spots
                        DetectBrightPoint(srcFrame, ref brightPoints, pupilRect);

                        // reset ROIs
                        Cv2.CopyTo(inputFrame, srcFrame);


                        Point2d topRight = new Point2d();
                        Point2d topLeft = new Point2d();
                        Point2d bottomRight = new Point2d();
                        Point2d bottomLeft = new Point2d();
                        // Select the 4 bright spots in the detected ones
                        SelectBrightPointFour(brightPoints, pupilCenter, ref topRight, ref topLeft, ref bottomRight, ref bottomLeft, pupilRect);

                        // If the bright spots are detected, use the 2 bottom ones to detect the center of the 4 (because same distance between the spots)
                        bool existBrightPoint = true;
                        if ((bottomRight.X == 0.0 && bottomRight.Y == 0.0) || (bottomLeft.X == 0.0 && bottomLeft.Y == 0.0))
                        {
                            existBrightPoint = false;
                        }

                        // gaze tracking starts
                        if (existBrightPoint)
                        {
                            double brightPointDistance = Math.Sqrt((bottomLeft.X - bottomRight.X) * (bottomLeft.X - bottomRight.X) + (bottomLeft.Y - bottomRight.Y) * (bottomLeft.Y - bottomRight.Y));
                            Point2d brightPointCenter = new Point2d((bottomLeft.X + bottomRight.X) / 2, (bottomLeft.Y + bottomRight.Y) / 2 - brightPointDistance / 2);
                            brightPointCenter.X = brightCenterXMedian.GetMedian(brightPointCenter.X + pupilRect.TopLeft.X);
                            brightPointCenter.Y = brightCenterYMedian.GetMedian(brightPointCenter.Y + pupilRect.TopLeft.Y);

                            //Adjust lens deviation
                            brightPointCenter.X = 1.1 * brightPointCenter.X - 0.1 * 640 / 2;
                            brightPointCenter.Y = 1.1 * brightPointCenter.Y - 0.1 * 480 / 2;
                            Cv2.Circle(outFrame, (int)brightPointCenter.X , (int)brightPointCenter.Y , 5, new Scalar(200, 200, 200), 1); // 輝点中心
                            Cv2.PutText(outFrame, "Bright Points Center (pxl): ("+Math.Round(brightPointCenter.X,2) + ","+ Math.Round(brightPointCenter.Y,2)+")", new Point(150, 400), HersheyFonts.HersheySimplex, 0.7, Scalar.Blue);
                            Cv2.PutText(outFrame, "PupilCenter (pxl): (" + Math.Round(pupilCenter.X,2) + "," + Math.Round(pupilCenter.Y,2) + ")", new Point(150, 420), HersheyFonts.HersheySimplex, 0.7, Scalar.Blue);

                            if (debug == true)
                            {
                                List<Point2d> selectedBrightPoints = new List<Point2d>();
                                selectedBrightPoints.Add(topRight);
                                selectedBrightPoints.Add(topLeft);
                                selectedBrightPoints.Add(bottomLeft);
                                selectedBrightPoints.Add(bottomRight);

                                DrawBrightPoints(outFrame, selectedBrightPoints, pupilRect);
                            }
                            if (m_IsValidInitialOffset)
                            {
                                gaze_X_deg = -((brightPointCenter.X - pupilCenter.X - ptStOffsetX) * 1.230);
                                gaze_Y_deg = ((brightPointCenter.Y - pupilCenter.Y - ptStOffsetY) * 1.230);
                                Cv2.PutText(outFrame, "Gaze (deg): (" + Math.Round(gaze_X_deg,2)  + "," + Math.Round(gaze_Y_deg,2) + ")", new Point(150, 440), HersheyFonts.HersheySimplex, 0.7, Scalar.Blue);
                            }

                            offsetListX[offsetListPos] = brightPointCenter.X - pupilCenter.X;
                            offsetListY[offsetListPos] = brightPointCenter.Y - pupilCenter.Y;
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
                                    int n = ValidFrameCount % 40;
                                    pupilCenterListX[n] = pupilCenter.X;
                                    pupilCenterListY[n] = pupilCenter.Y;
                                    brightCenterListX[n] = brightPointCenter.X;
                                    brightCenterListY[n] = brightPointCenter.Y;
                                    ValidFrameCount++;

                                    if (ValidFrameCount >= 40)
                                    {
                                        for (int i = 0; i < 40; i++)
                                        {
                                            initOffsetListX[i] = brightCenterListX[i] - pupilCenterListX[i];
                                            initOffsetListY[i] = brightCenterListY[i] - pupilCenterListY[i];
                                        }
                                        double averageInitOffsetX = initOffsetListX.Average();
                                        double averageInitOffsetY = initOffsetListY.Average();

                                        double varianceInitOffsetX = 0.0;
                                        double varianceInitOffsetY = 0.0;
                                        for (int i = 0; i < 40; i++)
                                        {
                                            varianceInitOffsetX += (initOffsetListX[i] - averageInitOffsetX) * (initOffsetListX[i] - averageInitOffsetX);
                                            varianceInitOffsetY += (initOffsetListY[i] - averageInitOffsetY) * (initOffsetListY[i] - averageInitOffsetY);
                                        }
                                        varianceInitOffsetX /= 40.0;
                                        varianceInitOffsetY /= 40.0;

                                        if (varianceInitOffsetX + varianceInitOffsetY < 0.64)
                                        {
                                            ptStOffsetX = averageInitOffsetX;
                                            ptStOffsetY = averageInitOffsetY;

                                            m_IsValidInitialOffset = true;
                                        }
                                    }
                                    if (ValidFrameCount >= 100)
                                    {
                                        isMeasureStart = false;
                                    }
                                }
                                else
                                {
                                    ValidFrameCount = 0;
                                }
                                preValidFrameCount = frameCount;
                            }
                        }
                        else
                        {
                            pupilWidthHeight = 0.0;
                            gaze_X_deg = 0;
                            gaze_Y_deg = 0;
                            pupilRect = new Rect(0, 0, inputFrame.Width, inputFrame.Height);
                        }

                        pupilWidthHeight = (int)pupilDiameter;
                    }
                    else
                    {
                        pupilWidthHeight = 0.0;
                        gaze_X_deg = 0;
                        gaze_Y_deg = 0;
                        pupilRect = new Rect(0, 0, inputFrame.Width, inputFrame.Height);
                    }
*/
    }

    private boolean DetectPupil(Mat InputROI, Mat dstMat, double pupilDiameter, 
                                Point pupilCenter, RotatedRect ellipse, Rect pupilRect, Mat rawFrame) {

        // Rough detection of the pupil
        int pupilColorUpperLimit = 80;
        int houghCirclesThreshold = 80;

        boolean result = false;
        //Mat tempMatforReset;
        //InputROI.copyTo(tempMatforReset);

        Mat circles = new Mat();
            // Houghcircles does not need to be binarized yet. "4" is the grid for accuracy (the smaller the more accurate, but the more misdetections)
        Imgproc.cvtColor(InputROI, InputROI, Imgproc.COLOR_RGB2GRAY, 0);
        Imgproc.HoughCircles(InputROI, circles, Imgproc.HOUGH_GRADIENT, 2, 500, houghCirclesThreshold, 40, 15, 30);

        System.out.println("Found some circles: " + circles.size());

        return(false);
            /* 
            // if no circles detected, stop and reset pupilRect to srcMat size.
            if (circles.Count() == 0)
            {
                pupilDiameter = 0;
                pupilRect = new Rect(0, 0, rawFrame.Width, rawFrame.Height);
                return false;
            }
            List<Tuple<double, double, double>> pupilList = new List<Tuple<double, double, double>>();
            // Create a list of all the circles detected, and change the coordinates to the absolute frame
            foreach (CircleSegment circle in circles)
            {
                pupilList.Add(new Tuple<double, double, double>(circle.Center.X + pupilRect.TopLeft.X, circle.Center.Y + pupilRect.TopLeft.Y, circle.Radius));
            }

            // Then, check if next to the detected circle, the colors are as expected:
            // If the detected circle is a pupil, the color on its left en right should be lower than the center of the circle, 
            // if not, it is probably not the pupil. White -> Black -> White
            // If few circles meet the expectations, then check if the black is really black.
            List<Tuple<double, double, double>> pupil = new List<Tuple<double, double, double>>();
            int pupilColorC = 255; // Center
            int pupilColorR = 255; // Right
            int pupilColorL = 255; // Left

            foreach (Tuple<double, double, double> pupilListitem in pupilList)
            {
                int x = (int)pupilListitem.Item1;
                int y = (int)pupilListitem.Item2;
                int r = (int)pupilListitem.Item3;
                if (InputROI.Width == 120)
                {
                    x = (int)pupilListitem.Item1 - pupilRect.TopLeft.X;
                    y = (int)pupilListitem.Item2 - pupilRect.TopLeft.Y;
                }
                r = r > 25 ? 25 : r;
                // Cv2.PutText(dstMat, "ROI: " + InputROI.Size().ToString(), new Point(10, 40), HersheyFonts.HersheySimplex, 1, Scalar.Red);
                var centerColor = GetColorValue(InputROI, y, x - r, x + r);
                var leftsideColor = GetColorValue(InputROI, y, x - 2 * r - 2, x - 2 * r + 3);
                var rightsideColor = GetColorValue(InputROI, y, x + 2 * r - 2, x + 2 * r + 3);
                int mincolor = rightsideColor < leftsideColor ? rightsideColor : leftsideColor;
                double threshold1 = (centerColor + mincolor) / 2.0;
                //Cv2.PutText(dstMat, threshold1.ToString(), new Point(10, 200), HersheyFonts.HersheySimplex, 1, Scalar.Yellow);
                // Cv2.PutText(dstMat, centerColor.ToString() + "," + leftsideColor.ToString() + "," + rightsideColor.ToString(), new Point(10, 200), HersheyFonts.HersheySimplex, 1, Scalar.Yellow );


                if (centerColor <= pupilColorUpperLimit)
                {
                    result = true;//
                    if (pupilColorC > centerColor)
                    {
                        pupilColorC = centerColor;
                        pupilColorR = rightsideColor;
                        pupilColorL = leftsideColor;
                        pupil.Add(pupilListitem);
                    }
                }

                //if (InputROI.Width > 120)
                //{
                //    Cv2.Circle(dstMat, new Point(x, y), r, Scalar.Blue, 2);
                //}

            }

            if (result)
            {
                int edge = 60;
                newROIRect = new Rect((int)(pupil[0].Item1 - edge), (int)(pupil[0].Item2 - edge), 2 * edge, 2 * edge);
                if (debug)
                {
                    Cv2.Rectangle(dstMat, newROIRect, Scalar.Red);
                }

                // if (!(pupilRect.X <= newROIRect.X + newROIRect.Width && newROIRect.X <= rawFrame.Width && pupilRect.Y <= newROIRect.Y + newROIRect.Height && newROIRect.Y <= rawFrame.Height))
                if (!(0 <= newROIRect.X + newROIRect.Width && newROIRect.X <= rawFrame.Width && 0 <= newROIRect.Y + newROIRect.Height && newROIRect.Y <= rawFrame.Height)) 
                {
                    pupilDiameter = 0;
                    pupilRect = new Rect(0, 0, rawFrame.Width, rawFrame.Height);

                    return false;
                }
                // reset roi
                Cv2.CopyTo(rawFrame, InputROI);


                // set new roi
                InputROI = new Mat(InputROI, newROIRect);

                Point2d gravityCenter = new Point2d();
                Point2d pupilSize = new Point2d();

                // Calculate the minimum between pupil color right and left.
                // If (pupilColoreR < pupilColoreL == true) -> pupilColoreR. Else{ pupilColoreL}
                // then, set the threshold as the mean value between center and minimum
                int pupilColorMin = pupilColorR < pupilColorL ? pupilColorR : pupilColorL;

                double threshold = (pupilColorC + pupilColorMin) / 2.0;
                //Cv2.PutText(dstMat, threshold.ToString(), new Point(10, 100), HersheyFonts.HersheySimplex, 1, Scalar.Yellow );

                // Cv2.PutText(dstMat, "(" + pupil[0].Item1 + "," + pupil[0].Item2 + "," + (int)pupil[0].Item3 + ")", new Point(newROIRect.X, newROIRect.Y), HersheyFonts.HersheyDuplex, 1, Scalar.Black);
                //threshold = 100;
                //binarization
                Cv2.Threshold(InputROI, InputROI, threshold, 255, ThresholdTypes.BinaryInv);

                //List<Point2d> points;
                // Get all the input informations with getPupilInfo. (use FindContour)
                Cv2.CvtColor(InputROI, InputROI, ColorConversionCodes.RGB2GRAY);
                result = GetPupilInfo(InputROI, ref dstMat, ref gravityCenter, ref pupilCenter, ref pupilSize, ref ellipse, newROIRect);
                if (result)
                {
                    pupilDiameter = pupilSize.Y * 14.0 / 176.0;
                    pupilRect = new Rect((int)(pupilCenter.X - edge), (int)(pupilCenter.Y - edge), 2 * edge, 2 * edge);
                    //if (debug)
                    //{
                    //    //Cv2.Rectangle(dstMat, pupilRect, Scalar.Red);
                    //}
                    if (!(0 <= pupilRect.X + pupilRect.Width && pupilRect.X <= rawFrame.Width && 0 <= pupilRect.Y + pupilRect.Height && pupilRect.Y <= rawFrame.Height))
                    {
                        pupilDiameter = 0;
                        pupilRect = new Rect(0, 0, rawFrame.Width, rawFrame.Height);
                        return false;
                    }
                }
                if (debug)
                {
                    //// center area
                    //Cv2.Circle(dstMat, (int)(pupil[0].Item1 - pupil[0].Item3), (int)pupil[0].Item2, 4, Scalar.Yellow);
                    //Cv2.Circle(dstMat, (int)(pupil[0].Item1 + pupil[0].Item3), (int)pupil[0].Item2, 4, Scalar.Yellow);

                    ////Left checked area
                    //Cv2.Circle(dstMat, (int)(pupil[0].Item1 - 2 * pupil[0].Item3 - 2), (int)pupil[0].Item2, 4, Scalar.Red);
                    //Cv2.Circle(dstMat, (int)(pupil[0].Item1 - 2 * pupil[0].Item3 + 3), (int)pupil[0].Item2, 4, Scalar.Blue);
                    //// Right checked area
                    //Cv2.Circle(dstMat, (int)(pupil[0].Item1 + 2 * pupil[0].Item3 - 2), (int)pupil[0].Item2, 4, Scalar.Red);
                    //Cv2.Circle(dstMat, (int)(pupil[0].Item1 + 2 * pupil[0].Item3 + 3), (int)pupil[0].Item2, 4, Scalar.Blue);
                }
            }
            return result;
        }
            */

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
     * @param socket Open socket on which to write byets
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
}