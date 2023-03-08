package org.lei.opi.jovp;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;

import org.lei.opi.core.Display;
import org.lei.opi.core.OpiListener;
import org.lei.opi.core.Packet;

import org.junit.jupiter.api.Test;

import es.optocom.jovp.Controller;

/**
 *
 * Unitary tests for commumication with IMO driver
 *
 * @since 0.0.1
 */
public class JovpServerConnectionTests {

  /**
   *
   * Open monitor, get local ID and port, and close it
   *
   * @since 0.0.1
   */
  @Test
  public void getIDAndPort() {
    OpiJovp server = new OpiJovp(50002);
    System.out.println("[getIDAndPort] " + server);
  }

  /**
   *
   * Open connection, change local port, and close connection to Display driver
   *
   * @since 0.0.1
   */
  @Test
  public void changeLocalPort() {
    OpiJovp server = new OpiJovp(50003);
    System.out.println("[changeLocalPort] Address was at " + server);
    server = new OpiJovp(50008);
    System.out.println("[changeLocalPort] Address is at " + server);
  }

  /**
   *
   * List of suitable USB serial controllers attached to the computer
   *
   * @since 0.0.1
   */
  @Test
  public void listUsbControllers() {
    System.out.print("Suitable USB Controllers: ");
    System.out.println(Arrays.toString(Controller.getSuitableControllers()));
  }

  /**
   *
   * Open monitor, send initialise message, get reply.
   * Note 1 need opi_settings.json visible in the jovp root dir for this test to run.
   * Note 2 need vulkan and JOVP installed too
   * @since 0.2.0
   */
  @Test
  public void testInitialise() {
    OpiJovp server = new OpiJovp(50002);
    System.out.println("[testInitialise] " + server);

    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("[testInitialise] Waiting 2 seconds...");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }

        try {
          Display machine = new Display(null);
          System.out.println("[testInitialise] " + machine);

          if (machine != null && !machine.connect(server.getIP(), server.getPort()))
            System.out.println(String.format("[testInitialise] Cannot connect to %s:%s", server.getIP(), server.getPort()));
          else
            System.out.println(String.format("[testInitialise] Connected to %s:%s", server.getIP(), server.getPort()));

          Packet result = machine.initialize(null);
          System.out.println(String.format("[testInitialise] %s", result));

          try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }

          result = machine.close(); 
          System.out.println(String.format("[testInitialise] %s", result));
        } catch (InstantiationException e) {
          System.out.println("Probably couldn't connect Display to JOVP");
          e.printStackTrace();
        }

      }
    });

    t.start();
    server.startPsychoEngine();
    try { t.join(); } catch (InterruptedException ignored) { ; }
  }

  /**
   * Open monitor, send initialise message, get reply, send query, get reply, close.
   * Note 1 need opi_settings.json visible in the jovp root dir for this test to run.
   * Note 2 need vulkan and JOVP installed too
   * @since 0.2.0
   */
  @Test
  public void testInitialiseQuery() {
    OpiJovp server = new OpiJovp(50002);
    System.out.println("[testInitialiseQuery] " + server);

    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("[testInitialiseQuery] Waiting 2 seconds...");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }
        try {
          Display machine = new Display(null);
          System.out.println("[testInitialiseQuery] " + machine);

          if (machine != null && !machine.connect(server.getIP(), server.getPort()))
            System.out.println(String.format("[testInitialiseQuery] Cannot connect to %s:%s", server.getIP(), server.getPort()));
          else
            System.out.println(String.format("[testInitialiseQuery] Connected to %s:%s", server.getIP(), server.getPort()));

          Packet result = machine.initialize(null);
          System.out.println(String.format("[testInitialiseQuery] %s", result));

          try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }

          result = machine.query();
          System.out.println(String.format("[testInitialiseQuery] %s", result));

          try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }

          result = machine.close(); 
          System.out.println(String.format("[testInitialiseQuery] %s", result));
        } catch (InstantiationException e) {
          System.out.println("Probably couldn't connect Display to JOVP");
          e.printStackTrace();
        }
      }
    });

    t.start();
    server.startPsychoEngine();
    try { t.join(); } catch (InterruptedException ignored) { ; }
  }

  /**
   * Open monitor, send initialise message, get reply, setup, get reply, close.
   * Note 1 need opi_settings.json visible in the jovp root dir for this test to run.
   * Note 2 need vulkan and JOVP installed too
   * @since 0.2.0
   */
  @Test
  public void testInitialiseSetup() {
    OpiJovp server = new OpiJovp(50002);
    System.out.println("[testInitialiseSetup] " + server);

    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("[testInitialiseSetup] Waiting 2 seconds...");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }

        try {
          Display machine = new Display(null);
          System.out.println("[testInitialiseSetup] " + machine);

          if (machine != null && !machine.connect(server.getIP(), server.getPort()))
            System.out.println(String.format("[testInitialiseSetup] Cannot connect to %s:%s", server.getIP(), server.getPort()));
          else
            System.out.println(String.format("[testInitialiseSetup] Connected to %s:%s", server.getIP(), server.getPort()));

          Packet result = machine.initialize(null);
          System.out.println(String.format("[testInitialiseSetup] %s", result));

          try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }

          Packet qResult = machine.query();
          HashMap<String, Object> hmap = OpiListener.gson.fromJson((String)qResult.getMsg(), HashMap.class);
          hmap.put("eye", "BOTH");
          hmap.put("bgLum", 10.0);
          hmap.put("bgCol", new ArrayList<Double>() {{add(0.5);add(0.5);add(0.5);}});
          hmap.put("fixShape", "CROSS");
          hmap.put("fixLum", 50.0);
          hmap.put("fixCol", new ArrayList<Double>() {{add(0.0);add(1.0);add(0.5);}});
          hmap.put("fixCx", 0.0);
          hmap.put("fixCy", 0.0);
          hmap.put("fixSx", 1.0);
          hmap.put("fixSy", 1.0);
          hmap.put("fixRotation", 0.0);
          hmap.put("tracking", 0.0);
          result = machine.setup(hmap);
          System.out.println(String.format("[testInitialiseSetup] %s", result));

          try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }

          result = machine.close(); 
          System.out.println(String.format("[testInitialiseSetup] %s", result));
        } catch (InstantiationException e) {
          System.out.println("Probably couldn't connect Display to JOVP");
          e.printStackTrace();
        }
      }
    });

    t.start();
    server.startPsychoEngine();
    try { t.join(); } catch (InterruptedException ignored) { ; }
  }

  /**
   * Open monitor, send initialise message, get reply, setup, get reply, close.
   * Note 1 need opi_settings.json visible in the jovp root dir for this test to run.
   * Note 2 need vulkan and JOVP installed too
   * @since 0.2.0
   */
  @Test
  public void testInitialiseSetupPresent() {
    OpiJovp server = new OpiJovp(50002);
    System.out.println("[testInitialiseSetupPresent] " + server);

    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("[testInitialiseSetupPresent] Waiting 2 seconds...");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }
          
        try {
          Display machine = new Display(null);
          System.out.println("[testInitialiseSetupPresent] " + machine);

          if (machine != null && !machine.connect(server.getIP(), server.getPort()))
            System.out.println(String.format("[testInitialiseSetupPresent] Cannot connect to %s:%s", server.getIP(), server.getPort()));
          else
            System.out.println(String.format("[testInitialiseSetupPresent] Connected to %s:%s", server.getIP(), server.getPort()));

          Packet result = machine.initialize(null);
          System.out.println(String.format("[testInitialiseSetupPresent] %s", result));

          try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }

          Packet qResult = machine.query();
          HashMap<String, Object> hmap = OpiListener.gson.fromJson((String)qResult.getMsg(), HashMap.class);
          hmap.put("eye", "BOTH");
          hmap.put("bgLum", 10.0);
          hmap.put("bgCol", new ArrayList<Double>() {{add(0.5);add(0.5);add(0.5);}});
          hmap.put("fixShape", "CROSS");
          hmap.put("fixLum", 50.0);
          hmap.put("fixCol", new ArrayList<Double>() {{add(0.0);add(1.0);add(0.5);}});
          hmap.put("fixCx", 0.0);
          hmap.put("fixCy", 0.0);
          hmap.put("fixSx", 1.0);
          hmap.put("fixSy", 1.0);
          hmap.put("fixRotation", 0.0);
          hmap.put("tracking", 0.0);
          result = machine.setup(hmap);
          System.out.println(String.format("[testInitialiseSetupPresent] %s", result));

          HashMap stim = makeStimulus();
          result = machine.present(stim);
          System.out.println(String.format("[testInitialiseSetupPresent] %s", result));

          try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }

          result = machine.close(); 
          System.out.println(String.format("[testInitialiseSetupPresent] %s", result));
        } catch (InstantiationException e) {
          System.out.println("Probably couldn't connect Display to JOVP");
          e.printStackTrace();
        }
      }
    });

    t.start();
    server.startPsychoEngine();
    try { t.join(); } catch (InterruptedException ignored) { ; }
  }

  private HashMap<String, Object> makeStimulus() {
    ArrayList<Double> col = new ArrayList<Double>() { {add(1.0); add(1.0); add(1.0);}};

    HashMap<String, Object> hmap = new HashMap<String, Object>();
    hmap.put("eye", new ArrayList<String>() { {add("left");}});
    hmap.put("shape", new ArrayList<String>() { {add("circle");}});
    hmap.put("type", new ArrayList<String>() { {add("flat");}});
    hmap.put("x", new ArrayList<Double>() { {add(-3.0);}});
    hmap.put("y", new ArrayList<Double>() { {add(-3.0);}});
    hmap.put("sx", new ArrayList<Double>() { {add(1.72);}});
    hmap.put("sy", new ArrayList<Double>() { {add(1.72);}});
    hmap.put("lum", new ArrayList<Double>() { {add(20.0);}});
    hmap.put("color1", new ArrayList<ArrayList<Double>>() { {add(col);}});
    hmap.put("color2", new ArrayList<ArrayList<Double>>() { {add(col);}});
    hmap.put("rotation", new ArrayList<Double>() { {add(0.0);}});
    hmap.put("contrast", new ArrayList<Double>() { {add(1.0);}});
    hmap.put("phase", new ArrayList<Double>() { {add(1.0);}});
    hmap.put("frequency", new ArrayList<Double>() { {add(1.0);}});
    hmap.put("defocus", new ArrayList<Double>() { {add(0.0);}});
    hmap.put("texRotation", new ArrayList<Double>() { {add(0.0);}});
    hmap.put("t", new ArrayList<Double>() { {add(200.0);}});
    hmap.put("w", new ArrayList<Double>() { {add(1500.0);}});

    return hmap;
  }
}