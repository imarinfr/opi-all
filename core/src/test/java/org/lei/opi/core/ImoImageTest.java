package org.lei.opi.core;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.*;
import org.opencv.videoio.*;

import org.junit.jupiter.api.Test;

public class ImoImageTest {
    /**
    * @since 0.3.0
    */
    private CameraStreamerImo cameraStreamer;
    
    ImoImageTest() {
        nu.pattern.OpenCV.loadLocally();  // works on mac and windows it seems
        try {
            cameraStreamer = new CameraStreamerImo(-1, new int[] {1});
        } catch (IOException e) {
        }
    }

    /** 
     * Call `processFrame` on test images
     */
    @Test
    public void detectPupil_images() throws IOException {
        for (int eye = 0; eye < 20; eye++) {
            long start = System.currentTimeMillis();
            String fname = String.format("/org/lei/opi/core/ImoVifa/eye_%02d.jpg", eye);
            BufferedImage im = ImageIO.read(getClass().getResource(fname));

            Mat frame = new Mat(im.getHeight(), im.getWidth(), CvType.CV_8UC3);

            byte[] im_array = ((DataBufferByte) im.getRaster().getDataBuffer()).getData();
            frame.put(0, 0, im_array);

            System.out.print("\nProcessFrame: " + eye);

            long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            cameraStreamer.getImageValues(frame);
            long mem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            

            if (cameraStreamer.pupilInfo.valid)
                System.out.println(String.format("%3d %10d Found pupil:" + cameraStreamer.pupilInfo, System.currentTimeMillis() - start, mem2 - mem1)) ;
            else
                System.out.println(String.format("%3d %10d No Pupil found", System.currentTimeMillis() - start, mem2 - mem1));
        }
    }

    /** 
     * Call `processFrame` on a stream of images.
     */
    @Test
    public void detectPupil_vidImages() {
        //String fname = this.getClass().getResource("/org/lei/opi/core/ImoVifa/eye_00.jpg").toString();
        //System.out.println("         Filename: " + fname);
        //fname = fname.replace("00", "%02d");
        //System.out.println("Sequence Filename: " + fname);
        VideoCapture camera = new VideoCapture(0); //, Videoio.CAP_IMAGES);

        if (!camera.isOpened()) {
            System.out.println("Error! Camera can't be opened!");
            return;
        }
        Mat frame = new Mat();

        while (camera.isOpened()) {
            if (camera.read(frame)) {
                cameraStreamer.getImageValues(frame);
                if (cameraStreamer.pupilInfo.valid)
                    System.out.println("Pupil:" + cameraStreamer.getResults());
                else
                    System.out.println("No Pupil found");
            } else
                System.out.println("Error! Could not read frame");
        }
    }
}