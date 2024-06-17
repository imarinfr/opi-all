package org.lei.opi.core;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

// NOTE This cannot be run on a machine without a camera as Consumer can never be satisfied.
//      (eg OpiMachine.Display on a mac as that does not allow access to the camera.)
public class CameraQueueTest {
    final int camerPort = 50202;

    /**
    * @since 0.3.0
    */
    class Consumer extends Thread {
        CameraStreamer server;
        
        Consumer(CameraStreamer server) { this.server = server; }

        @Override
        public void run() {
            System.out.println("[sendAndReceive1]...Consume starting");
            
            try {
                int tries = 20;
                while (!isInterrupted()) {
                    CameraStreamer.Response resp = null;
                    int count = 0;
                    while (resp == null && count < tries) {
                        resp = server.responseQueue.poll(50, TimeUnit.MILLISECONDS);
                        count++;
                    }
                    if (count == tries)
                        System.out.println("[sendAndReceive1]...Consumer tried too many times.");
                    else
                        System.out.println(String.format("[sendAndReceive1]...Consumer got %s. Time delta %4s. (%6.2f,%6.2f) %4.2f mm",
                            resp.requestTimeStamp(),
                            resp.acquisitionTimeStamp() - resp.requestTimeStamp(),
                            resp.x(), resp.y(), resp.diameter()));
                }
            } catch (InterruptedException e) { ; }
        }
    }

    class Producer extends Thread {
        CameraStreamer server;
        Producer(CameraStreamer server) { this.server = server; }

        @Override
        public void run() {
            System.out.println("[sendAndReceive1]...Produce starting");
            
            try {
                while (!isInterrupted()) {
                    CameraStreamer.Request req = new CameraStreamer.Request(System.currentTimeMillis(), 1);
                    server.requestQueue.put(req);
                    System.out.println(String.format("[sendAndReceive1]...Prodcuer issued request: %s.", req.timeStamp));
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) { ; }
        }
    }

    class ImageSaver {
        Socket socket = null;
        CameraStreamer server = null;
        int numImages;
        public ImageSaver(CameraStreamer server, int numImages) {
            this.server = server;
            this.numImages = numImages;

            int tries = 0;
            while (socket == null && tries < 10)
                try {
                    socket = new Socket("localhost", camerPort);
                } catch (IOException e) {
                    tries++;
                    System.out.println("JovpQueueTest is waiting for eye camera socket on port: " + camerPort);
                    try { Thread.sleep(2000); } catch (InterruptedException ee) { ; }
                }

            if (tries >= 10)
                System.out.println("JovpQueueTest is giving up on ImageSaver thread.");
            else
                run();
        }

        public void run() {
            int savedCount = 0;
            while (savedCount < numImages) {
                server.readBytes(socket);
                BufferedImage image = new BufferedImage(640, 480, BufferedImage.TYPE_3BYTE_BGR);
                byte[] im_array = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
                try {
                    CameraStreamerImo.bytesLock.lock();
                    System.arraycopy(CameraStreamerImo.bytes, 0, im_array, 0, im_array.length);
                } finally {
                    CameraStreamerImo.bytesLock.unlock();
                }
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

    @Test
    public void sendAndReceive1() {
        nu.pattern.OpenCV.loadLocally();  // works on mac
        CameraStreamer server = null;
        try {
            server = new CameraStreamerImo(camerPort, new int[] {1});
        } catch (IOException e) {
            System.out.println("[sendAndReceive1]...Could not start camera");
            e.printStackTrace();
            return;
        }

        Producer p = new Producer(server);
        Consumer c = new Consumer(server);

        p.start();
        c.start();

       //ImageSaver i = new ImageSaver(server, 100); // write images to files

        try { p.join(); } catch (InterruptedException ignored) { ; }
        try { c.join(); } catch (InterruptedException ignored) { ; }
    }
}