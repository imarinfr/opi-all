package org.lei.opi.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

class TestDisplay {
    
@Test
public void testInit() {
    Display machine = null;
    try {
        machine = new Display(null);
    } catch (InstantiationException e) {
        System.out.println("Could not connect to JOVP server");
    }
    // Needs JOVP server running
    if (!machine.connect(machine.getSettings().ip, machine.getSettings().port)) {
        System.out.println("Could not connect to JOVP server");
    }

    int test = 1;
    if (test == 0) { init(machine); sleep(1); close(machine); }
    if (test == 1) { init(machine); sleep(1); present(machine) ; close(machine); }
}

// assumes JOVP server running
void init(Jovp machine) {
    String s = "{" + 
        "\"command\":\"initialize\"," + 
        String.format("\"ip\": \"%s\",", OpiListener.obtainPublicAddress().getHostAddress()) + 
        "\"port\":50001" +
    "}";
    HashMap<String, Object> hmap = OpiListener.jsonToPairs(s);
    System.out.println(machine.processPairs(hmap));
}

// assumes JOVP server running
void close(Jovp machine) {
    String s = "{" + "\"command\":\"close\"" + "}";
    HashMap<String, Object> hmap = OpiListener.jsonToPairs(s);
    System.out.println(machine.processPairs(hmap));
}

void sleep(int seconds) { try {Thread.sleep(seconds * 1000);} catch (InterruptedException ignored) { ; } } 

// assumes JOVP server running
void present(Jovp machine) {
    String s = "{" + 
        "\"command\":\"present\"," + 
        "\"sx\"     : [1.72]," +
        "\"lum\"    : [20.0]," +
        "\"stim.length\" : 1," + 
        "\"eye\"    : [\"LEFT\"], " + 
        "\"color1\" : [[1.0, 1.0, 1.0]]," + 
        "\"t\"      : [200.0]," + 
        "\"w\"      : 1500.0," + 
        "\"x\"      : [0.0], " + 
        "\"y\"      : [0.0]" + 
    "}";
    HashMap<String, Object> hmap = OpiListener.jsonToPairs(s);
    System.out.println(machine.processPairs(hmap));
}
}