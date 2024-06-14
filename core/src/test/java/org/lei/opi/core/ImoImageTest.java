package org.lei.opi.core;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.*;

import org.junit.jupiter.api.Test;

public class ImoImageTest {
    /**
    * @since 0.3.0
    */
    private CameraStreamerImo cameraStreamer;
    
    ImoImageTest() {
        try {
            cameraStreamer = new CameraStreamerImo(-1, new int[] {1});
        } catch (IOException e) {
        }
    }

    @Test
    public void detectPupil_image() throws IOException {
        //nu.pattern.OpenCV.loadShared();  // works on mac
        nu.pattern.OpenCV.loadLocally();  // works on mac and windows it seems
        
        BufferedImage im = ImageIO.read(getClass().getResource("/org/lei/opi/core/ImoVifa/eye_00.jpg"));
        byte[] im_array = ((DataBufferByte) im.getRaster().getDataBuffer()).getData();
        Mat frame2 = new Mat(im.getHeight(), im.getWidth(), CvType.CV_8UC3);
        frame2.put(0, 0, im_array);

        cameraStreamer.processFrame(frame2);
    }

    @Test
    public void detectPupil_vidImages() {
        nu.pattern.OpenCV.loadLocally();
        String fname = this.getClass().getResource("/org/lei/opi/core/imo_eye_OD.jpg").toString();
        System.out.println("Filename: " + fname);
        fname = fname.replace("imo_eye_OD", "%02d");
        System.out.println("Filename: " + fname);
        VideoCapture camera = new VideoCapture(fname, Videoio.CAP_IMAGES);

        if (!camera.isOpened()) {
            System.out.println("Error! Camera can't be opened!");
            return;
        }
        Mat frame = new Mat();

        if (camera.read(frame))
            cameraStreamer.processFrame(frame);
        else
            System.out.println("Error! Could not read frame");
    }
}