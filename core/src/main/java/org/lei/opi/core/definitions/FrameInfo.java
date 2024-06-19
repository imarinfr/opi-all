package org.lei.opi.core.definitions;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
/* Hold and manipulate info and about frame 
 *
 * @author Andrew Turpin
 * @date 5 June 2024 
 */
public class FrameInfo {
    private long timeStamp;
    private Mat mat;

    public FrameInfo() {
        this.timeStamp = -1;
        this.mat = new Mat();
    }

    public long timeStamp() { return timeStamp; }
    public Mat mat() { return mat; }

    /** 
     * @param dst Destination Mat to receive a copy of me
     * Copy myself into dst 
     */
    public void copyTo(FrameInfo dst) {
        dst.timeStamp = this.timeStamp;
        this.mat.copyTo(dst.mat);
    }

    /**
     * Grab a frame from the grabber and put it in {@link mat}.
     * @param grabber
     */
    public void grab(VideoCapture grabber) {
        this.timeStamp = System.currentTimeMillis(); 
        if (!grabber.read(this.mat))
            this.timeStamp = -1;
    }

    /**
     * Grab a frame from file filename and put it in {@link mat}.
     * @param grabber
     */
    public void grab(String filename) {
        try {
            //long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            BufferedImage im = ImageIO.read(getClass().getResource(filename));
            //long mem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            //System.out.println(mem2 - mem1);

            if (mat.type() != CvType.CV_8UC3) {
                mat.release();
                mat = new Mat(im.getHeight(), im.getWidth(), CvType.CV_8UC3);
            }

            byte[] im_array = ((DataBufferByte) im.getRaster().getDataBuffer()).getData();
            this.mat.put(0, 0, im_array);
            this.timeStamp = System.currentTimeMillis(); 
        } catch (IOException e) {
            this.timeStamp = -1;
            System.out.println("Error! Could not read frame from " + filename);
        }
    }

    public String toString() {
        return String.format("FrameInfo: timeStamp=%10d, mat=%s", timeStamp, mat);
    }
}