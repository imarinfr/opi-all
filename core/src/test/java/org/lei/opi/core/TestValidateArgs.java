package org.lei.opi.core;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import org.lei.opi.core.OpiMachine.MethodData;

public class TestValidateArgs {

void testPresent(String s) {
    HashMap<String, Object> pairs = OpiListener.jsonToPairs(s);

    try {
        Display m = new Display(null);
        MethodData methodData = m.opiMethods.get("present");
        System.out.println(m.validateArgs(pairs, methodData.parameters(), "present"));
    } catch (InstantiationException e) {
        System.out.println("Bummer.");
    }
}

@Test
void testPresent1() {
    testPresent(
    "{" + 
    "\"command\":\"present\"," + 
    "\"sx\"     : [1.72]," +
    "\"lum\"    : [20.0]," +
    "\"length\" : 1," + 
    "\"eye\"    : [\"LEFT\"], " + 
    "\"color1\" : [[1.0, 1.0, 1.0]]," + 
    "\"t\"      : [200.0]," + 
    "\"w\"      : 1500.0," + 
    "\"x\"      : [0.0], " + 
    "\"y\"      : [0.0]" + 
    "}"
    );
}

@Test
void testPresent2() {
    testPresent(
    "{" + 
    "\"command\":\"present\"," + 
    "\"sx\"     : [1.72]," +
    "\"lum\"    : [20.0]," +
    "\"length\" : 1," + 
    "\"eye\"    : [\"LEFT\"], " + 
    "\"color1\" : [[1.0, 1.0, 1.0]]," + 
    "\"t\"      : [200.0]," + 
    "\"w\"      : [1500.0]," +     // <<- error here
    "\"x\"      : [0.0], " + 
    "\"y\"      : [0.0]" + 
    "}"
    );
}
}
