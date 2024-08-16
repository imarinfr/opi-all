package org.lei.opi.jovp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import org.lei.opi.core.Display;
import org.lei.opi.core.ImoVifa;
import org.lei.opi.core.Jovp;
import org.lei.opi.core.OpiMachine;
import org.lei.opi.core.OpiListener.Command;
import org.lei.opi.core.definitions.Packet;
import org.lei.opi.core.definitions.Parameter;

import es.optocom.jovp.definitions.TextureType;

import org.junit.jupiter.api.Test;

public class JovpImageStimTest {

    /**
    * Open monitor, send initialise message, try presenting an image
    * Note 1 need opi_settings.json visible in the jovp root dir for this test to run.
    * Note 2 need vulkan and JOVP installed too
    * @since 0.2.0
    */
    
    @Test
    public void setupBgLumAndCol_Display() {
        nu.pattern.OpenCV.loadLocally();  // works on mac and windows it seems

        OpiJovp server = new OpiJovp(50002);
        System.out.println("[testSetupBgLumAndCol] " + server);
       
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
            System.out.println("[testSetupBgLumAndCol] Waiting 2 seconds...");
            try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }
              
            try {
                  // initialise()
                Display machine = new Display(null);
                System.out.println("[testSetupBgLumAndCol] " + machine);
                if (machine != null && !machine.connect(server.getIP(), server.getPort()))
                  System.out.println(String.format("[testSetupBgLumAndCol] Cannot connect to %s:%s", server.getIP(), server.getPort()));
                else
                  System.out.println(String.format("[testSetupBgLumAndCol] Connected to %s:%s", server.getIP(), server.getPort()));
               
                Packet result = machine.initialize(null);
                System.out.println(String.format("[testSetupBgLumAndCol] Initialize result %s", result));
               
                try { Thread.sleep(1000); } catch (InterruptedException ignored) { ; }
               
                HashMap<String, Object> setupArgs = getDefaultValues(Command.SETUP);
                setupArgs.put("bgLum", 200.0);
               
                result = machine.setup(setupArgs);
                System.out.println(String.format("[testSetupBgLumAndCol] Setup result: %s", result));
       
                HashMap<String, Object> stimArgs = getDefaultValues(Command.PRESENT);
                stimArgs.put("x", new ArrayList<>(List.of(3.0)));
                stimArgs.put("y", new ArrayList<>(List.of(3.0)));
                stimArgs.put("sx", new ArrayList<>(List.of(16.0)));
                stimArgs.put("sy", new ArrayList<>(List.of(6.0)));
                stimArgs.put("type", new ArrayList<>(List.of(TextureType.IMAGE)));
                stimArgs.put("imageFilename", new ArrayList<>(List.of("x.jpg")));
                stimArgs.put("t", new ArrayList<>(List.of(4000.0)));
                stimArgs.put("w", 5000.0);
                result = machine.present(stimArgs); 
                System.out.println(String.format("[testSetupBgLumAndCol] Present result: %s", result));
               
                result = machine.close(); 
                System.out.println(String.format("[testSetupBgLumAndCol] %s", result));
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
    public void setupBgLumAndCol_imoVifa() {
        nu.pattern.OpenCV.loadLocally();  // works on mac and windows it seems

        OpiJovp server = new OpiJovp(50002);
        System.out.println("[testSetupBgLumAndCol] " + server);
       
        Thread t = new Thread(new Runnable() {
          @Override
          public void run() {
            System.out.println("[testSetupBgLumAndCol] Waiting 2 seconds...");
            try { Thread.sleep(2000); } catch (InterruptedException ignored) { ; }
              
            try {
                  // initialise()
                ImoVifa machine = new ImoVifa(null);
                System.out.println("[testSetupBgLumAndCol] " + machine);
                if (machine != null && !machine.connect(server.getIP(), server.getPort()))
                  System.out.println(String.format("[testSetupBgLumAndCol] Cannot connect to %s:%s", server.getIP(), server.getPort()));
                else
                  System.out.println(String.format("[testSetupBgLumAndCol] Connected to %s:%s", server.getIP(), server.getPort()));
               
                Packet result = machine.initialize(null);
                System.out.println(String.format("[testSetupBgLumAndCol] Initialize result %s", result));
               
                try { Thread.sleep(1000); } catch (InterruptedException ignored) { ; }
               
                HashMap<String, Object> setupArgs = getDefaultValues(Command.SETUP);
                setupArgs.put("bgLum", 200.0);
                setupArgs.put("screen",  1);
                setupArgs.put("physicalSize",  new double[] {121, 68});
                setupArgs.put("fullScreen",  true);
                setupArgs.put("distance",  44);
                setupArgs.put("input",  "COM10");
                setupArgs.put("tracking",  0);
                setupArgs.put("gammaFile",  "imo_invGamma.json");
                setupArgs.put("deviceNumberCameraLeft",  1);
                setupArgs.put("deviceNumberCameraRight",  2);
                setupArgs.put("eyeStreamPort",  50200);
               
                result = machine.setup(setupArgs);
                System.out.println(String.format("[testSetupBgLumAndCol] Setup result: %s", result));
       
                HashMap<String, Object> stimArgs = getDefaultValues(Command.PRESENT);
                stimArgs.put("x", new ArrayList<>(List.of(3.0)));
                stimArgs.put("y", new ArrayList<>(List.of(3.0)));
                stimArgs.put("sx", new ArrayList<>(List.of(16.0)));
                stimArgs.put("sy", new ArrayList<>(List.of(6.0)));
                stimArgs.put("type", new ArrayList<>(List.of(TextureType.IMAGE)));
                stimArgs.put("imageFilename", new ArrayList<>(List.of("x.jpg")));
                stimArgs.put("t", new ArrayList<>(List.of(4000.0)));
                stimArgs.put("w", 5000.0);
                result = machine.present(stimArgs); 
                System.out.println(String.format("[testSetupBgLumAndCol] Present result: %s", result));
               
                result = machine.close(); 
                System.out.println(String.format("[testSetupBgLumAndCol] %s", result));
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