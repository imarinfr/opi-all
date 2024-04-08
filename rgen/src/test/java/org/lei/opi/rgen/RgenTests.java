package org.lei.opi.rgen;

import org.junit.jupiter.api.Test;
import org.lei.opi.core.Display;

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
  public void opiFunctionTest1() {
    try {
      OpiFunction f = new OpiFunction(new Display(null), "opiInitialise", "initialize", "", "list(err = %s)", true, true);
      f.generateR(System.out);
    } catch (InstantiationException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void opiFunctionTest2() {
    try {
      OpiFunction f = new OpiFunction(new Display(null), "opiInitializzze", "initialize", "", "list(err = %s)", true, false);
      f.generateR(System.out);
    } catch (InstantiationException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void opiFunctionTest3() {
    try {
      OpiFunction f = new OpiFunction(new Display(null), "opiPresent",   "present", "stim", "list(err=%s, seen=%s, time=%s", false, true);
      f.generateR(System.out);
    } catch (InstantiationException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void opiFunctionTest4() {
    try {
      OpiFunction f = new OpiFunction(new Display(null), "opiSetup",   "setup", "settings", "list(err=%s, seen=%s, time=%s", false, false);
      f.generateR(System.out);
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
