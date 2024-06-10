package org.lei.opi.jovp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import org.lei.opi.core.Display;
import org.lei.opi.core.Jovp;
import org.lei.opi.core.OpiMachine;
import org.lei.opi.core.OpiListener.Command;
import org.lei.opi.core.definitions.Packet;
import org.lei.opi.core.definitions.Parameter;

import org.junit.jupiter.api.Test;

public class JovpBgTest {

  /**
   * Open monitor, send initialise message, loop setting background lum then col
   * Note 1 need opi_settings.json visible in the jovp root dir for this test to run.
   * Note 2 need vulkan and JOVP installed too
   * @since 0.2.0
   */

  @Test
  public void aTestSetupBgLumAndCol() {
    OpiJovp server = new OpiJovp(50002);
    System.out.println("[testInitialiseSetupPresent] " + server);

    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        System.out.println("[testInitialiseSetupPresent] Waiting 2 seconds...");
        try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }
          
        try {
            // initialise()
          Display machine = new Display(null);
          System.out.println("[testInitialiseSetupPresent] " + machine);
          if (machine != null && !machine.connect(server.getIP(), server.getPort()))
            System.out.println(String.format("[testInitialiseSetupPresent] Cannot connect to %s:%s", server.getIP(), server.getPort()));
          else
            System.out.println(String.format("[testInitialiseSetupPresent] Connected to %s:%s", server.getIP(), server.getPort()));

          Packet result = machine.initialize(null);
          System.out.println(String.format("[testInitialiseSetupPresent] Initialize result %s", result));

          try { Thread.sleep(1000); } catch (InterruptedException ignored) { ; }

          HashMap<String, Object> setupArgs = getDefaultValues(Command.SETUP);

          for (double bgLum : List.of(0, 50, 100, 150, 200, 250, 300)) {
            setupArgs.put("bgLum", bgLum);

            result = machine.setup(setupArgs);
            System.out.println(String.format("[testInitialiseSetupPresent] Setup result: %s", result));

            try { Thread.sleep(1000); } catch (InterruptedException ignored) { ; }
          }

          for (double r : List.of(0.0, 0.5, 1.0))
          for (double b : List.of(0.0, 0.5, 1.0))
          for (double g : List.of(0.0, 0.5, 1.0)) {
            ArrayList<Double> a = new ArrayList<Double>();
            a.add(r); a.add(b); a.add(g);
            setupArgs.put("bgCol", a);

            result = machine.setup(setupArgs);
            System.out.println(String.format("[testInitialiseSetupPresent] Setup result: %s", result));

            try { Thread.sleep(500); } catch (InterruptedException ignored) { ; }
          }

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
    
  // get all of the default values from core.Jovp::setup()
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