package org.lei.opi.core;

import java.io.IOException;
import java.util.Arrays;

import org.opencv.core.*;
import org.opencv.videoio.*;

import org.junit.jupiter.api.Test;

public class ImoImageTest {
    /**
    * @since 0.3.0
    */
    private CameraStreamerImo cameraStreamer;
    
    ImoImageTest() {
        Arrays.asList(System.getProperty("java.class.path").split(":")).forEach(s -> System.out.println(s));

        try {
            cameraStreamer = new CameraStreamerImo(-1, new int[] {1});
        } catch (IOException e) {
        }
    }

    @Test
    public void detectPupil() {
        VideoCapture camera = new VideoCapture(this.getClass().getResource("/org/lei/opi/core/imo_eye_OD.jpg").toString());

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