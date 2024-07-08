package org.lei.opi.core.definitions;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
/* Hold and manipulate info and about frame 
 *
 * @author Andrew Turpin
 * @date 5 June 2024 
 */
public abstract class FrameInfo {
    protected long timeStamp;
    protected Mat mat;
    protected boolean hasPupil;
    protected int pupilDiameter;     // diameter of pupil in pixels
    protected int pupilX;      // x position of pupil in pixels from image centre
    protected int pupilY;      // y position of pupil in pixels from image centre

    public FrameInfo() {
        this.timeStamp = -1;
        this.mat = new Mat();
        this.hasPupil = false;
    }

    public FrameInfo(Mat m, long timeStamp) {
        this.timeStamp = timeStamp;
        this.mat = m;
        this.hasPupil = false;
    }

    public int pupilDiameter() { return pupilDiameter; }
    public int pupilY() { return pupilY; }
    public int pupilX() { return pupilX; }
    public boolean hasPupil() { return hasPupil; }
    public long timeStamp() { return timeStamp; }
    public Mat mat() { return mat; }

    /**
     * Copy my pupil info to another FrameInfo object.
     * @param destination
     */
    public <T extends FrameInfo> void copyPupilInfo(T destination) {
        destination.hasPupil = hasPupil;
        destination.pupilDiameter = pupilDiameter;
        destination.pupilX = pupilX;
        destination.pupilY = pupilY;
        destination.timeStamp = timeStamp;
    }

    /**
     * Grab a frame from the grabber and put it in {@link mat}.
     * @param grabber
     */
    public void grab(VideoCapture grabber) throws IOException {
        this.timeStamp = System.currentTimeMillis(); 
        this.hasPupil = false;
        if (!grabber.read(this.mat)) {
            this.timeStamp = -1;
            throw new IOException("Failed to grab a frame from " + grabber.toString());
        }
    }

    /*
     * Grab image from file.
     * @param f file to grab from
     */
    public void grab(final File f) throws IOException {
        this.hasPupil = false;
        this.timeStamp = -1;

        final BufferedImage im = ImageIO.read(f);

        if (this.mat.width() != im.getWidth() || this.mat.height() != im.getHeight() || this.mat.channels() != 3)
            this.mat = new Mat(im.getHeight(), im.getWidth(), CvType.CV_8UC3);

        final byte[] im_array = ((DataBufferByte) im.getRaster().getDataBuffer()).getData();
        this.mat.put(0, 0, im_array);

        this.timeStamp = System.currentTimeMillis(); 
    }

        // Used for test files within package
    public void grab(String filename) throws IOException {
        try {
            File f = new File(getClass().getResource(filename).getFile());
            grab(f);
        } catch (NullPointerException e) {
            this.timeStamp = -1;
            this.hasPupil = false;
            throw new FileNotFoundException("Error! Could not read frame from " + filename);
        }
    }

    public String toString() {
        if (this.hasPupil)
            return String.format("FrameInfo: timeStamp=%10d, mat=%s x: %+3d, y: %+3d, d: %3d", timeStamp, mat, pupilX, pupilY, pupilDiameter);
        else
            return String.format("FrameInfo: timeStamp=%10d, mat=%s No pupil", timeStamp, mat);
    }

    /*
    * Fill in the pupil data for the mat in this object.
    */
    public abstract void findPupil();
}