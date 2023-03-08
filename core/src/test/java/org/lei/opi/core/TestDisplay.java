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
        e.printStackTrace();
        return;
    }
    //machine.connect();

    HashMap<String, Object> hmap = new HashMap<String, Object>();
    
    hmap.put("command", "initialize");
    hmap.put("ip", OpiListener.obtainPublicAddress().getHostAddress());
    hmap.put("port", 50001);

    System.out.println(machine.processPairs(hmap));
}

@Test
public void testPresent() {
    Echo machine = new Echo(null);

    HashMap<String, Object> hmap = new HashMap<String, Object>();
    
    hmap.put("command", "present");

    System.out.println(machine.processPairs(hmap));
}


}