package org.lei.opi.core;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.lei.opi.core.definitions.FrameInfo;
import org.lei.opi.core.definitions.PupilRequest;
import org.lei.opi.core.definitions.PupilResponse;

import es.optocom.jovp.definitions.ViewEye;

// NOTE This cannot be run on a machine without a camera as Consumer can never be satisfied.
//      (eg OpiMachine.Display on a mac as that does not allow access to the camera.)
public class CameraQueueTest {
    final int cameraPort = 50202;

    /**
    * @since 0.3.0
    */
    class Consumer extends Thread {
        CameraStreamer<? extends FrameInfo> server;
        
        Consumer(CameraStreamer<? extends FrameInfo> server) { this.server = server; }

        @Override
        public void run() {
            System.out.println("[sendAndReceive1]...Consume starting");
            
            try {
                int tries = 20;
                while (!isInterrupted()) {
                    PupilResponse resp = null;
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
        CameraStreamer<? extends FrameInfo> server;
        Producer(CameraStreamer<? extends FrameInfo> server) { this.server = server; }

        @Override
        public void run() {
            System.out.println("[sendAndReceive1]...Produce starting");
            
            try {
                while (!isInterrupted()) {
                    PupilRequest req = new PupilRequest(System.currentTimeMillis(), ViewEye.LEFT);
                    server.requestQueue.put(req);
                    System.out.println(String.format("[sendAndReceive1]...Producer issued request: %s.", req.timeStamp()));
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) { ; }
        }
    }

    @Test
    public void sendAndReceive1() {
        nu.pattern.OpenCV.loadLocally();  // works on mac
        CameraStreamer<? extends FrameInfo> server = null;
        try {
            server = new CameraStreamerImo(cameraPort, 1, -1);
        } catch (IOException e) {
            System.out.println("[sendAndReceive1]...Could not start camera");
            e.printStackTrace();
            return;
        }

        Producer p = new Producer(server);
        Consumer c = new Consumer(server);

        p.start();
        c.start();

        try { p.join(); } catch (InterruptedException ignored) { ; }
        try { c.join(); } catch (InterruptedException ignored) { ; }
    }
}