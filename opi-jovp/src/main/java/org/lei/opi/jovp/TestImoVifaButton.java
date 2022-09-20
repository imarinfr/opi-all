import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;
import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortEvent;

import java.util.stream.Stream;
import java.util.Arrays;
import java.util.stream.Collectors;

class TestImoVifaButton {

    TestImoVifaButton() { }

    class Listener implements SerialPortEventListener {
        private SerialPort p;

        public Listener(SerialPort p) { this.p = p; }

        public void serialEvent(SerialPortEvent event) {
            /*
            //System.out.println(e);
            if (event.isBREAK()) System.out.println("BREAK set");
            if (event.isCTS()) System.out.println("CTS set");
            if (event.isDSR()) System.out.println("DSR set");
            if (event.isERR()) System.out.println("ERR");
            if (event.isRING()) System.out.println("RING set");
            if (event.isRLSD()) System.out.println("RLSD set");
            if (event.isRXFLAG()) System.out.println("RXFLAG");
            if (event.isTXEMPTY()) System.out.println("TXEMPTY");

            if (event.isRXCHAR()) {
                try {
                    int n = p.getInputBufferBytesCount();
                    System.out.println("RXCHAR " + n);
                } catch (SerialPortException e) {
                    System.out.println("Cannot get bytes in Listener.");
                    System.out.println(e);
                    System.exit(-1);
                }
            }
            */
            try {
                if (event.isRXCHAR()) {
                    //int n = p.getInputBufferBytesCount();
                    //System.out.println("RXCHAR " + n);

                        // Code specifically for the ImoVifa button 
                        // On click it sends 5 bytes   (as ints: 42 79 78 78 35)
                        // On release it sends 5 bytes (as ints: 42 79 70 70 35)
                    int[] msg = p.readIntArray(5, 50);  // 5 bytes in ... ms
                    for (int i : msg)
                        System.out.print(i + " ");
                
                    if (msg[3] == 78) System.out.println("Pressed");
                    if (msg[3] == 70) System.out.println("Released");
                }
                //p.purgePort(SerialPort.PURGE_RXCLEAR | SerialPort.PURGE_TXCLEAR);
                //System.out.println("PRESS");
            } catch (SerialPortException e) {
                System.out.println("Cannot get bytes in Listener.");
                System.out.println(e);
                System.exit(-1);
            } catch (SerialPortTimeoutException e) {
                System.out.println("Didn't get 5 bytes");
            }

        }
    }


    /** Listen forever on the first port found. */
    private void run() {
        String[] names = SerialPortList.getPortNames();
        if (names.length == 0) {
            System.out.println("No ports found");
            System.exit(-1);
        }

        System.out.println(Stream.of(names).collect(Collectors.joining(",")));

        try {
            SerialPort p = new SerialPort(names[0]);
            p.openPort();
            p.addEventListener(new Listener(p));
            while (true) 
                Thread.sleep(500);
        } catch (InterruptedException e) {
            System.exit(0);
        } catch (SerialPortException e) {
            System.out.println("Cannot add listener");
            System.out.println(e);
            System.exit(-1);
        }
    }

    public static void main(String args[]) {
        TestImoVifaButton t = new TestImoVifaButton();
        t.run();
    }
}