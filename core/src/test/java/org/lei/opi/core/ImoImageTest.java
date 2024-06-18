package org.lei.opi.core;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

import org.opencv.core.*;
import org.opencv.videoio.*;

import org.junit.jupiter.api.Test;
import org.lei.opi.core.definitions.CircularBuffer;
import org.lei.opi.core.definitions.FrameInfo;

public class ImoImageTest {
    /**
    * @since 0.3.0
    */
    private CameraStreamerImo cameraStreamer;
    final int cameraPort = 50202;

    ImoImageTest() {
        nu.pattern.OpenCV.loadLocally();  // works on mac and windows it seems
        try {
            cameraStreamer = new CameraStreamerImo(cameraPort, new int[] {1});
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
                File outputfile = new File(String.format("eye_%02d.jpg", savedCount));
                try {
                    ImageIO.write(image, "jpg", outputfile);
                    System.out.println("Wrote file: " + outputfile.getAbsolutePath());
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
     * Can't get this to work..
    @Test
     */
    public void detectPupil_vidImages() {
        String fname = this.getClass().getClassLoader().getResource("org/lei/opi/core/ImoVifa/eye_00.jpg").toString();
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

      /** 
     * Call `processFrame` on a stream of images.
     */
    @Test
    public void detectPupil_video() {

        for (int i = 0 ; i < 30 ; i++) {
            System.out.println("Request " + i);
            try {
                cameraStreamer.requestQueue.add(new CameraStreamer.Request(System.currentTimeMillis(), 1));
                CameraStreamer.Response resp = cameraStreamer.responseQueue.poll();
                System.out.println(resp);
                Thread.sleep(300); 
            } catch (InterruptedException e) { break; }
            catch (Exception e) { ; }
        }
    }

    @Test
    public void circular_buffer_test() throws IOException {
        CameraStreamerImo cameraStreamer = new CameraStreamerImo(-1, new int[] {1});
        CircularBuffer<FrameInfo> buffer = cameraStreamer.frameBuffer[0];

        FrameInfo workingFrameInfo = new FrameInfo();

        for (int eye = 0; eye < 30; eye++) {
            String fnameMut = String.format("/org/lei/opi/core/ImoVifa/eye_%02d.jpg", eye);
            if (eye >= 20)
                fnameMut = String.format("/org/lei/opi/core/ImoVifa/eye_%02d.jpg", eye - 20);
            String fname = fnameMut;

            System.out.println("\nProcessFrame: " + eye);

            long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            buffer.put(f -> f.grab(fname));
            long mem2 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            //System.out.println("\t Buffer: " + buffer);

            long start = System.currentTimeMillis();
            System.out.println("Looking at: " + (start - 10));
            if (buffer.getHeadToTail((FrameInfo f) -> f.timeIsClose(start - 10, 50), (src, dst) -> src.copyTo(dst), workingFrameInfo)) {
                System.out.println("\t Got a frame");
                cameraStreamer.getImageValues(workingFrameInfo.mat());
                long mem3 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                System.out.println(String.format("\t\tt= %3d dt= %4d memGrab= %10d memProc= %10d Pupil:" + cameraStreamer.pupilInfo, 
                    System.currentTimeMillis() - start, 
                    start - workingFrameInfo.timeStamp(), 
                    mem2 - mem1, 
                    mem3 - mem2)) ;
            } else
                System.out.println("\t Dropped frame");
        }
    }
}