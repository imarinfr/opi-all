package org.lei.opi.jovp;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;

import org.lei.opi.core.Display;
import org.lei.opi.core.ImoVifa;
import org.lei.opi.core.Jovp;
import org.lei.opi.core.OpiMachine;
import org.lei.opi.core.OpiListener.Command;
import org.lei.opi.core.definitions.Packet;
import org.lei.opi.core.definitions.Parameter;

import org.junit.jupiter.api.Test;

import es.optocom.jovp.Controller;
import es.optocom.jovp.definitions.ViewEye;

/**
 *
 * Unitary tests for commumication with IMO driver
 *
 * @since 0.0.1
 */
public class JovpParamTest {

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

    @Test
    public void testSetupParam_Display1() {
        Jovp machine;
        try {
          machine= new Display(null);
        } catch (Exception e) {
            return;
        }

        ArrayList<Double> white = new ArrayList<Double>() { {add(1d); add(1d); add(1d);}};
        ArrayList<Double> green = new ArrayList<Double>() { {add(0d); add(1d); add(0d);}};

        HashMap<String, Object> args = new HashMap<String, Object>();

        Packet p;

        System.out.println("Minimal");
        args.put("eye", "Left");
        p = machine.validateArgs(args, machine.opiMethods.get("setup").parameters(), "setup");
        System.out.println(p);

        System.out.println("BgLum");
        args.put("bgLum", 255);
        args.put("bgCol", white);
        p = machine.validateArgs(args, machine.opiMethods.get("setup").parameters(), "setup");
        System.out.println(p);

        System.out.println("Bg Image");
        args.clear();
        args.put("eye", "Right");
        args.put("bgImageFilename", "/path/clouds1.jpg");
        p = machine.validateArgs(args, machine.opiMethods.get("setup").parameters(), "setup");
        System.out.println(p);
        args.put("eye", "Left");
        args.put("bgImageFilename", "/path/clouds2.jpg");
        p = machine.validateArgs(args, machine.opiMethods.get("setup").parameters(), "setup");
        System.out.println(p);

        System.out.println("Fixation");
        args.put("fixShape", "CROSS");
        args.put("fixCx", 0.0);
        args.put("fixCy", 0.0);
        args.put("fixSx", 0.0);
        args.put("fixLum", 0.0);
        args.put("fixCol", green);
        args.put("tracking", 0d);
        args.put("fixRotation", 0d);

        p = machine.validateArgs(args, machine.opiMethods.get("setup").parameters(), "setup");
        System.out.println(p);
    }

  /**
   *
   * Open monitor, send initialise message, get reply.
   * Note 1 need opi_settings.json visible in the jovp root dir for this test to run.
   * Note 2 need vulkan and JOVP installed too
   * @since 0.2.0
   */
  public void aTestInitialise() {
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
   * Open monitor, send initialise message, get reply, setup, get reply, close.
   * Note 1 need opi_settings.json visible in the jovp root dir for this test to run.
   * Note 2 need vulkan and JOVP installed too
   * @since 0.2.0
   */
  @Test
  public void aTestImageFixation() {
    OpiJovp server = new OpiJovp(50002);
    System.out.println("[testImageFixation] " + server);

    Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
            System.out.println("[testImageFixation] Waiting 2 seconds...");
            try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }
           
            try {
                Display machine = new Display(null);
                System.out.println("[testImageFixation] " + machine);
               
                if (machine != null && !machine.connect(server.getIP(), server.getPort()))
                    System.out.println(String.format("[testImageFixation] Cannot connect to %s:%s", server.getIP(), server.getPort()));
                else
                    System.out.println(String.format("[testImageFixation] Connected to %s:%s", server.getIP(), server.getPort()));
                
                HashMap<String, Object> args = new HashMap<String, Object>(){{ put("screen", 1);}};
                Packet result = machine.initialize(args);
                System.out.println(String.format("[testImageFixation] %s", result));
               
                try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }
               
                HashMap<String, Object> hmap = getDefaultValues(Command.SETUP);
                hmap.put("fixShape", "CIRCLE");
                hmap.put("fixType", "IMAGE");
                hmap.put("fixImageFilename", "../jovp/x.jpg");
                hmap.put("bgLum", 200.0);
                result = machine.setup(hmap);
                System.out.println(String.format("[testImageFixation] Setup result: %s", result));
               
                System.out.println("[testImageFixation] sleeping 20s");
                try { Thread.sleep(20000); } catch (InterruptedException ignored) { ; }
               
                result = machine.close(); 
                System.out.println(String.format("[testImageFixation] Close result: %s", result));
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
  public void aTestInitialiseSetup() {
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

          HashMap<String, Object> hmap = getDefaultValues(Command.SETUP);
          hmap.replace("bgCol", new ArrayList<>(Arrays.asList(1.0, 0.0, 0.0)));
          result = machine.setup(hmap);
          System.out.println(String.format("[testInitialiseSetup] Setup result: %s", result));

          System.out.println("[testInitialiseSetup] sleeping 20s");
          try { Thread.sleep(20000); } catch (InterruptedException ignored) { ; }

          result = machine.close(); 
          System.out.println(String.format("[testInitialiseSetup] Close result: %s", result));
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
   * Open monitor, send initialise message, get result from Query
   * Note 1 need opi_settings.json visible in the jovp root dir for this test to run.
   * Note 2 need vulkan and JOVP installed too
   * @since 0.2.0
   */
  @Test
  public void aTestInitialiseQuery() {
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
          
          HashMap<String, Object> args = new HashMap<String, Object>(){{ put("screen", 1);}};
          Packet result = machine.initialize(args);
          System.out.println(String.format("[testInitialiseQuery] %s", result));

          try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }

          result = machine.query();
          System.out.println(String.format("[testInitialiseQuery] Query result: %s", result));

          try { Thread.sleep(20000); } catch (InterruptedException ignored) { ; }

          result = machine.close(); 
          System.out.println(String.format("[testInitialiseQuery] Close result: %s", result));
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
   * Open monitor, send initialise message, setup, present 2item stim, close.
   * Note 1 need opi_settings.json visible in the jovp root dir for this test to run.
   * Note 2 need vulkan and JOVP installed too
   * @since 0.2.0
   */
  @Test
  public void aTestPresent2() {
    OpiJovp server = new OpiJovp(50002);
    System.out.println("[aTestPresent2] " + server);

    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("[aTestPresent2] Waiting 2 seconds...");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }
          
        try {
          Display machine = new Display(null);
          //ImoVifa machine = new ImoVifa(null);
          System.out.println("[aTestPresent2] " + machine);
          if (machine != null && !machine.connect(server.getIP(), server.getPort()))
            System.out.println(String.format("[aTestPresent2] Cannot connect to %s:%s", server.getIP(), server.getPort()));
          else
            System.out.println(String.format("[aTestPresent2] Connected to %s:%s", server.getIP(), server.getPort()));

          Packet result = machine.initialize(null);
          System.out.println(String.format("[aTestPresent2] Initialize result %s", result));

          try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }

          HashMap<String, Object> setupArgs = getDefaultValues(Command.SETUP);
          result = machine.setup(setupArgs);
          System.out.println(String.format("[aTestPresent2] Setup result: %s", result));

          ArrayList<ArrayList<Double>> cols = new ArrayList<ArrayList<Double>>(
            Arrays.asList(
              new ArrayList<Double>(Arrays.asList(1.0, 0.0, 1.0)),
              new ArrayList<Double>(Arrays.asList(0.0, 1.0, 0.0))
            )
          );

          HashMap<String, Object> stimArgs = new HashMap<String, Object>();
          stimArgs.put("stim.length", 2.0);
          stimArgs.put("eye", new ArrayList<String>(Arrays.asList("left", "right")));
          stimArgs.put("x", new ArrayList<Double>(Arrays.asList(1.0, -10.0)));
          stimArgs.put("y", new ArrayList<Double>(Arrays.asList(1.0, 10.0)));
          stimArgs.put("sx", new ArrayList<Double>(Arrays.asList(1.0, 1.0)));
          stimArgs.put("sy", new ArrayList<Double>(Arrays.asList(1.0, 1.0)));
          stimArgs.put("lum", new ArrayList<Double>(Arrays.asList(255.0, 255.0)));
          stimArgs.put("color1", cols);
          stimArgs.put("t", new ArrayList<Double>(Arrays.asList(0.0, 1000.0)));
          stimArgs.put("w", 1005.0);
          stimArgs.put("command", "present");

          for (int i = 0 ; i < 5 ; i++) {
            result = machine.present(stimArgs);
            System.out.println(String.format("[aTestPresent2] %s", result));
          }

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
  /**
   * Open monitor, send initialise message, get reply, setup, get reply, close.
   * Note 1 need opi_settings.json visible in the jovp root dir for this test to run.
   * Note 2 need vulkan and JOVP installed too
   * @since 0.2.0
   */
  @Test
  public void aTestInitialiseSetupPresent() {
    OpiJovp server = new OpiJovp(50002);
    System.out.println("[testInitialiseSetupPresent] " + server);

    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("[testInitialiseSetupPresent] Waiting 2 seconds...");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }
          
        try {
          Display machine = new Display(null);
          //ImoVifa machine = new ImoVifa(null);
          System.out.println("[testInitialiseSetupPresent] " + machine);
          if (machine != null && !machine.connect(server.getIP(), server.getPort()))
            System.out.println(String.format("[testInitialiseSetupPresent] Cannot connect to %s:%s", server.getIP(), server.getPort()));
          else
            System.out.println(String.format("[testInitialiseSetupPresent] Connected to %s:%s", server.getIP(), server.getPort()));

          Packet result = machine.initialize(null);
          System.out.println(String.format("[testInitialiseSetupPresent] Initialize result %s", result));

          try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }

            // setup()....present()
          for (int t = 0 ; t <= 2 ; t++) {
            System.out.println("\n-------------- Test Number: " + t);
            HashMap<String, Object> setupArgs = getDefaultValues(Command.SETUP);
            HashMap<String, Object> stimArgs = getDefaultValues(Command.PRESENT);
            stimArgs.replace("sx", new ArrayList<Double>(Arrays.asList(0.43)));
            stimArgs.replace("sy", new ArrayList<Double>(Arrays.asList(0.43)));
            stimArgs.replace("lum", new ArrayList<Double>(Arrays.asList(3000.0)));
            stimArgs.replace("t", new ArrayList<Double>(Arrays.asList(10000.0)));
            stimArgs.replace("w", 10600.0);
            switch(t) {
                case 1: setupArgs.remove("fixCol"); break;
                case 2: stimArgs.remove("stim.length"); break;
            }
            if (t == 0) {
              result = machine.setup(setupArgs);
              System.out.println(String.format("[testInitialiseSetupPresent] Setup result: %s", result));
            }

            //HashMap stim = makeStimulus();
            ArrayList<String> a = new ArrayList<String>();
            a.add("right");
            stimArgs.put("eye", a);
            result = machine.present(stimArgs);
            System.out.println(String.format("[testInitialiseSetupPresent] %s", result));
          }

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

  @Test
  public void aTestPresentPixel() {
    OpiJovp server = new OpiJovp(50002);
    System.out.println("[testPresentPixel] " + server);

    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("[testPresentPixel] Waiting 2 seconds...");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }
          
        try {
            // initialise()
          Display machine = new Display(null);
          System.out.println("[testPresentPixel] " + machine);
          if (machine != null && !machine.connect(server.getIP(), server.getPort()))
            System.out.println(String.format("[testPresentPixel] Cannot connect to %s:%s", server.getIP(), server.getPort()));
          else
            System.out.println(String.format("[testPresentPixel] Connected to %s:%s", server.getIP(), server.getPort()));

          Packet result = machine.initialize(null);
          System.out.println(String.format("[testPresentPixel] Initialize result %s", result));

          try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }

            // setup
          HashMap<String, Object> setupArgs = getDefaultValues(Command.SETUP);
          result = machine.setup(setupArgs);
          System.out.println(String.format("[testPresentPixel] Setup result: %s", result));

            // present
          for (int t = 0 ; t <= 6 ; t++) {
            System.out.println("\n-------------- Test Number: " + t);
            HashMap<String, Object> stimArgs = getDefaultValues(Command.PRESENT);

            //HashMap stim = makeStimulus();
            stimArgs.put("eye", new ArrayList<String>(Arrays.asList(new String[] {"right"})));
            stimArgs.put("units", new ArrayList<String>(Arrays.asList(new String[] {"PIXELS"})));
            stimArgs.put("sx", new ArrayList<Double>(Arrays.asList(new Double[] {18.34 * 0.43})));
            stimArgs.put("sy", new ArrayList<Double>(Arrays.asList(new Double[] {18.34 * 0.43})));
            stimArgs.put("x", new ArrayList<Double>(Arrays.asList(new Double[] {18.34 * t * 5})));
            stimArgs.put("y", new ArrayList<Double>(Arrays.asList(new Double[] {18.34 * t * 5})));
            result = machine.present(stimArgs);
            System.out.println(String.format("[testPresentPixel] %s", result));
          }

          try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }

          result = machine.close(); 
          System.out.println(String.format("[testPresentPixel] %s", result));
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

  // get all of the default values from core.Jovp::c()
  public HashMap<String, Object> getDefaultValues(Command c) {
      HashMap<String, Object> hmap = new HashMap<String, Object>();
      hmap.put("command", c.toString());
      try {
        Method meth = Jovp.class.getDeclaredMethod(c.toString().toLowerCase(), HashMap.class);
        Method pMeth = OpiMachine.class.getDeclaredMethod(c.toString().toLowerCase(), HashMap.class);
        Parameter[] parameters = ArrayUtils.addAll(pMeth.getAnnotationsByType(Parameter.class),
                                                 meth.getAnnotationsByType(Parameter.class));
        for (Parameter p : parameters) {
          Object v = OpiMachine.buildDefault(p, 1);
          hmap.put(p.name(), v);
        }
      } catch (NoSuchMethodException | ClassNotFoundException e) {
        e.printStackTrace();
        return null;
      }
      return hmap;
  }
}