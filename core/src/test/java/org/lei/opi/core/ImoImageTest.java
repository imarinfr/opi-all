package org.lei.opi.core;

import java.io.IOException;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
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
    public void detectPupil_image() {
        nu.pattern.OpenCV.loadLocally();
        String fname = this.getClass().getResource("/org/lei/opi/core/imo_eye_OD.jpg").toString();
        System.out.println("Filename: " + fname);

        Mat frame = Imgcodecs.imread(fname);

        cameraStreamer.processFrame(frame);
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