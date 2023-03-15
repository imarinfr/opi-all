package org.lei.opi.jovp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

import org.lei.opi.core.Display;
import org.lei.opi.core.Jovp;
import org.lei.opi.core.OpiMachine;
import org.lei.opi.core.OpiListener.Command;
import org.lei.opi.core.definitions.Packet;
import org.lei.opi.core.definitions.Parameter;

import es.optocom.jovp.definitions.ModelType;

public class JovpStimTest {
    
  /**
   * Open monitor, send initialise message, setup, Loop over stim types 
   * Note 1 need opi_settings.json visible in the jovp root dir for this test to run.
   * Note 2 need vulkan and JOVP installed too
   * @since 0.2.0
   */
  @Test
  public void testStims1() {
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

          try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }

          HashMap<String, Object> setupArgs = getDefaultValues(Command.SETUP);
          setupArgs.put("bgLum", 100);
          setupArgs.put("fixLum", 200);

          result = machine.setup(setupArgs);
          System.out.println(String.format("[testInitialiseSetupPresent] Setup result: %s", result));

          HashMap<String, Object> stimArgs = getDefaultValues(Command.PRESENT);

          stimArgs.put("x", new ArrayList<>(List.of(3.0)));
          stimArgs.put("y", new ArrayList<>(List.of(3.0)));
          stimArgs.put("sx", new ArrayList<>(List.of(3.0)));
          stimArgs.put("sy", new ArrayList<>(List.of(3.0)));
          stimArgs.put("w", 300.0);

          for (double r : List.of(0.0, 1.0))
          for (double g : List.of(0.0, 1.0)) 
          for (double b : List.of(0.0, 1.0)) 
          for (ModelType shape : List.of(ModelType.CIRCLE, ModelType.SQUARE))
          for (double lum : List.of(50.0, 100.0, 150.0, 200.0, 250.0, 300.0)) {
            stimArgs.put("lum", new ArrayList<>(List.of(lum)));
            stimArgs.put("shape", new ArrayList<>(List.of(shape.name())));
            ArrayList<Double> a = new ArrayList<Double>();
            a.add(r); a.add(g); a.add(b);
            stimArgs.put("color1", new ArrayList<>(List.of(a)));
            stimArgs.put("color2", new ArrayList<>(List.of(a)));

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
