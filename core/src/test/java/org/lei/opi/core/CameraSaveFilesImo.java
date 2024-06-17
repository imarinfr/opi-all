package org.lei.opi.core;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

// NOTE This cannot be run on a machine without a camera as Consumer can never be satisfied.
//      (eg OpiMachine.Display on a mac as that does not allow access to the camera.)
public class CameraSaveFilesImo {
    final int camerPort = 50202;

    class ImageSaver {
        Socket socket = null;
        CameraStreamer server = null;
        public ImageSaver(CameraStreamer server) {
            this.server = server;

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
            while (savedCount < 20) {
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

        ImageSaver i = new ImageSaver(server); // write images to files
    }
}