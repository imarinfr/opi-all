package org.lei.opi.rgen;

import org.junit.jupiter.api.Test;
import org.lei.opi.core.Display;
import org.lei.opi.core.ImoVifa;

import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.lei.opi.core.definitions.Parameter;

/**
 *
 * Unitary tests for socket connections
 *
 * @since 0.0.1
 */
public class RgenTests {
  
  /**
   *
   * DUMMY description
   *
   * @since 0.0.1
   */
  @Test
  public void mainTest() {
    try {
      Main.makeR(new Display(null), System.out);
    } catch (InstantiationException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void opiDisplayInit1() {
    try {
      OpiFunction f = new OpiFunction(new Display(null), "opiInitialise", "initialize", "", "list(err = %s)", true, true);
      f.generateR(System.out, null, null);
    } catch (InstantiationException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void opiDisplayInit2() {
    try {
      OpiFunction f = new OpiFunction(new Display(null), "opiInitializzze", "initialize", "", "list(err = %s)", true, false);
      f.generateR(System.out, null, null);
    } catch (InstantiationException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void opiDisplayPresent1() {
    try {
      OpiFunction f = new OpiFunction(new Display(null), "opiPresent",   "present", "stim", "list(err=%s, seen=%s, time=%s", false, true);
      f.generateR(System.out, "ip=..., port=...", "blah blah");
    } catch (InstantiationException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void opiDisplaySetup1() {
    try {
      OpiFunction f = new OpiFunction(new Display(null), "opiSetup",   "setup", "settings", "list(err=%s, seen=%s, time=%s", false, false);
      f.generateR(System.out, "blah blah", null);
    } catch (InstantiationException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void opiImoVifaPresent1() {
    try {
      OpiFunction init = new OpiFunction(new ImoVifa(null), "opiInitialise",   "initialize", "", "list(err=%s", true, true);
      OpiFunction setup = new OpiFunction(new ImoVifa(null), "opiSetup",   "setup", "", "list(err=%s", false, false);
      OpiFunction f = new OpiFunction(new ImoVifa(null), "opiPresent",   "present", "stim", "list(err=%s, seen=%s, time=%s", false, true);
      f.generateR(System.out, init.callingExample, setup.callingExample);
    } catch (InstantiationException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void annotations() {
    try {
        Class<?> c = Class.forName("org.lei.opi.core.Display");
        while (c != null) {
            System.out.println(c);
            for (Method m : c.getMethods()) {
                System.out.println("\t" + m.getName());
                for (Parameter a : m.getAnnotationsByType(Parameter.class))
                    System.out.println("\t\t" + a.name());
            }

            c = c.getSuperclass();  // go up to parent
        }
    } catch (ClassNotFoundException e) { 
        e.printStackTrace(); 
    }

    String a[] = new String[] {"a", "b"};
    System.out.println("[" + 
      Stream.of(a)
      .map((Object o) -> "\"" + o.toString() + "\"")
      .collect(Collectors.joining(","))
     + "]");

     //ArrayList<String> b = new ArrayList<String>();
     //Object c = new ArrayList<String>();
     //Class <?> d = Class.forName("ArrayList<String>");
}

}
