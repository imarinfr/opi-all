package org.lei.opi.core;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

import org.opencv.core.*;

import es.optocom.jovp.definitions.ViewEye;

import org.junit.jupiter.api.Test;
import org.lei.opi.core.definitions.CircularBuffer;
import org.lei.opi.core.definitions.FrameInfoImo;
import org.lei.opi.core.definitions.PupilRequest;
import org.lei.opi.core.definitions.PupilResponse;

public class ImoImageTest {
    /**
    * @since 0.3.0
    */
    private CameraStreamerImo cameraStreamer;
    final int cameraPort = 50202;

    ImoImageTest() {
        nu.pattern.OpenCV.loadLocally();  // works on mac and windows it seems
        try {
            cameraStreamer = new CameraStreamerImo(cameraPort, 1, -1);
        } catch (IOException e) {
        }
    }

    class ImageSaver {
        Socket socket = null;
        public ImageSaver() {
            int tries = 0;
            while (socket == null && tries < 10) {
                System.out.println("ImageSaver: trying to connect to socket on port: " + cameraPort);
                try {
                    socket = new Socket("localhost", cameraPort);
                } catch (IOException e) {
                    tries++;
                    try { Thread.sleep(2000); } catch (InterruptedException ee) { ; }
                }
            }

            if (!socket.isConnected())
                System.out.println("ImageSaver:  giving up on socket.");
            else
                System.out.println("ImageSaver: connected socket.");
        }

        public void run() {
            if (!socket.isConnected()) {
                System.out.println("ImageSaver is giving up on ImageSaver thread.");
                return;
            }

           int savedCount = 0;
           BufferedImage image = new BufferedImage(640, 480, BufferedImage.TYPE_3BYTE_BGR);
           byte[] im_array = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            while (savedCount < 20) {
                System.out.println("ImageSaver: begin read: " + savedCount);
                cameraStreamer.readBytes(socket, im_array);
                System.out.println("ImageSaver: begin write: " + savedCount);
                File outputFile = new File(String.format("src/test/resources/eye_%02d.jpg", savedCount));
                try {
                    ImageIO.write(image, "jpg", outputFile);
                    System.out.println("Wrote file: " + outputFile.getAbsolutePath());
                } catch (IOException e) {
                    System.out.println("JovpQueueTest ImageSaver thread is having trouble saving images.");
                    e.printStackTrace();
                }
                savedCount++;
            }
        }
    }

    /** steam camera to files */
    @Test
    public void saveImageFilesImo() {
        ImageSaver i = new ImageSaver(); // write images to files
        i.run();
    }

    /** 
     * Call `processFrame` on test images
     */
    @Test
    public void detectPupil_images() throws IOException {
        for (int eye = 0; eye < 50; eye++) {
            long start = System.currentTimeMillis();
            String fname = String.format("/org/lei/opi/core/ImoVifa/Left/eye_%02d.jpg", eye);
            try {
                BufferedImage im = ImageIO.read(getClass().getResource(fname));

                Mat frame = new Mat(im.getHeight(), im.getWidth(), CvType.CV_8UC3);

                byte[] im_array = ((DataBufferByte) im.getRaster().getDataBuffer()).getData();
                frame.put(0, 0, im_array);

                System.out.print("\nProcessFrame: " + eye);
                FrameInfoImo f = new FrameInfoImo(frame, System.currentTimeMillis());

                long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                f.findPupil();
                long mem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                
                if (f.hasPupil())
                    System.out.println(String.format("t= %3d mem= %10d :" + f, System.currentTimeMillis() - start, mem2 - mem1)) ;
                else
                    System.out.println(String.format("t= %3d mem= %10d : No Pupil found", System.currentTimeMillis() - start, mem2 - mem1));
            } catch(IllegalArgumentException e) { ; }
        }
    }

    /** 
     * Call `processFrame` on a stream of images.
    Does not work
    @Test
    public void detectPupil_vidImages() {
        String fname = "/org/lei/opi/core/ImoVifa/eye_%02d.jpg";
        System.out.println("         Filename: " + fname);
        fname = fname.replace("00", "%02d");
        System.out.println("Sequence Filename: " + fname);
        VideoCapture camera = new VideoCapture(fname, Videoio.CAP_IMAGES);

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
     */

    /** 
     * Call `processFrame` on a stream of images.
     */
    @Test
    public void detectPupil_video() {

        for (int i = 0 ; i < 30 ; i++) {
            System.out.println("Request " + i);
            try {
                cameraStreamer.requestQueue.add(new PupilRequest(System.currentTimeMillis(), ViewEye.LEFT));
                PupilResponse resp = cameraStreamer.responseQueue.poll();
                System.out.println(resp);
                Thread.sleep(300); 
            } catch (InterruptedException e) { break; }
            catch (Exception e) { ; }
        }
    }

    @Test
    public void circular_buffer_test() throws IOException {
        CircularBuffer<FrameInfoImo> buffer = new CircularBuffer<FrameInfoImo>(FrameInfoImo::new, 30);

        FrameInfoImo workingFrameInfo = new FrameInfoImo();

        for (int eye = 0; eye < 100; eye++) {
            try {
                String fnameMut = String.format("/org/lei/opi/core/ImoVifa/Left/eye_%02d.jpg", eye);
                //if (eye >= 20)
                //    fnameMut = String.format("/org/lei/opi/core/ImoVifa/eye_%02d.jpg", eye - 20);
                String fname = fnameMut;

                System.out.println("\nProcessFrame: " + eye);

                long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                buffer.put(f -> f.grab(fname));
                buffer.applyHead(f -> f.findPupil());
                buffer.conditionalPop(f -> !f.hasPupil());
                long mem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                //System.out.println("\t Buffer: " + buffer);

                long start = System.currentTimeMillis();
                System.out.println("Looking at: " + (start - 10));
                if (buffer.getHeadToTail((FrameInfoImo f) -> Math.abs(f.timeStamp() - start + 10) < 50, (src, dst) -> src.copyPupilInfo(dst), workingFrameInfo)) {
                    System.out.println("\t Got a frame");
                    long mem3 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                    System.out.println(String.format("\t\tt= %3d dt= %4d memGrab= %10d memProc= %10d Pupil:" + workingFrameInfo, 
                        System.currentTimeMillis() - start, 
                        start - workingFrameInfo.timeStamp(), 
                        mem2 - mem1, 
                        mem3 - mem2)) ;
                } else
                    System.out.println("\t Dropped frame");
            } catch (IllegalArgumentException e) { ; } // missing file
        }
    }
}