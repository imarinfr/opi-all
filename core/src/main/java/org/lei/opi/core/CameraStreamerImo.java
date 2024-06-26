package org.lei.opi.core;

import java.io.IOException;
import java.net.Socket;
import java.util.ConcurrentModificationException;

import org.lei.opi.core.definitions.FrameInfo;
import org.opencv.core.*;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.features2d.SimpleBlobDetector_Params;
//import org.opencv.features2d.Features2d;
//import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;

import es.optocom.jovp.definitions.ViewEye;


/*
 * Contains custom pupil detection code using some constants from Csharp code supplied by CREWt 16 March 2023.
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

    SimpleBlobDetector_Params params = new 	SimpleBlobDetector_Params();
    SimpleBlobDetector blobDetector = SimpleBlobDetector.create(params);

    /** Weak instance just to allow calling of readBytes. Why not make it static? */
    public CameraStreamerImo() { ; }

    public CameraStreamerImo(int port, int deviceNumberLeft, int deviceNumberRight) throws IOException {
        super(port, deviceNumberLeft, deviceNumberRight);
        pupilInfo = new PupilInfo(EYE_IMAGE_WIDTH, EYE_IMAGE_HEIGHT);
        params.set_filterByArea(true);
        params.set_filterByCircularity(true);
        params.set_minThreshold(0);
        params.set_maxThreshold(COLOR_UPPER_LIMIT);
        params.set_minArea(100);
        params.set_maxArea(6000);
        params.set_minRepeatability(2);
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
    protected void getImageValues(Mat inputFrame) {
        //System.out.println(" " + inputFrame.size());
        //Imgcodecs.imwrite("input.jpg", inputFrame);

        Mat inputROI = new Mat(inputFrame, pupilRect);
        Imgproc.GaussianBlur(inputROI, inputROI, new Size(0, 0), 1.8);

        Imgproc.cvtColor(inputROI, inputROI, Imgproc.COLOR_RGB2GRAY, 0);

        //Imgcodecs.imwrite("roi_b4Blobs.jpg", inputROI);

        MatOfKeyPoint blobPoints = new MatOfKeyPoint();
        blobDetector.detect(inputROI, blobPoints);

        KeyPoint []ks = blobPoints.toArray();

        if (ks.length == 0) {
            pupilInfo.reset();
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
        pupilInfo.centerX = (ks[pupilIndex].pt.x + xAdj - EYE_IMAGE_WIDTH / 2.0) * 1.23;  // TODO need to allow for off centre start
        pupilInfo.centerY = (ks[pupilIndex].pt.y + yAdj - EYE_IMAGE_HEIGHT / 2.0) * 1.23;  // TODO need to allow for off centre start
        pupilInfo.diameter = ks[pupilIndex].size * 14.0 / 176.0;
        pupilInfo.valid = true;

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