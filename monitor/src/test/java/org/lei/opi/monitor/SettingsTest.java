package org.lei.opi.monitor;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.lei.opi.core.OpiMachine;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;

import org.lei.opi.core.Display;

/*
 * Test the read/write settings of Monitor
*/

class SettingsTest {

    /*
     * @param machineName The name of the machine to get settings for from its class and parents
     */
    @Test
    public void checkHeirachy() {
        String machineName = "Display";
        String className = machineName;
        Object currentSettingsObject = OpiMachine.fillSettings(className);
        while (currentSettingsObject == null && !className.equals("OpiMachine")) {
            try {
                Class<?> cls = Class.forName("org.lei.opi.core." + className);
                className = cls.getSuperclass().getSimpleName();
                currentSettingsObject = OpiMachine.fillSettings(className);
            } catch (ClassNotFoundException e) {
                System.out.println("Something is wrong - cannot find a Settings nested class for " + machineName);
                //e.printStackTrace();
            }
        }
    }

    @Test
    public void testReadSettingsFile() {
        String machineName = "Display";
        StringBuilder sb = new StringBuilder();
        try {
            HashMap<String, Object> map = OpiMachine.readSettingsFile();
            if (map.containsKey(machineName)) {
                @SuppressWarnings("unchecked")
                Map<String, Object> params = (Map<String, Object>)map.get(machineName);
                for (String p  : params.keySet()) {
                    List<StringProperty> line = new ArrayList<StringProperty>();
                    line.add(new SimpleStringProperty(p));
                    line.add(new SimpleStringProperty(params.get(p).toString()));
                    sb.append(line);
                    sb.append("\n");
                }
            } else {
                System.out.println("Could not find " + machineName + " in settings file, resetting to defauts.");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not find settings file, resetting to defauts.");
        }

        System.out.println(sb.toString());
    }

    @Test 
    public void createOpiMachine() {
        String machineName = "Display";
        try {
            Class<?> cls = Class.forName("org.lei.opi.core." + machineName);
            Constructor<?> ctor = cls.getConstructor(new Class[] {Scene.class, boolean.class});
            OpiMachine m = (OpiMachine)ctor.newInstance(null, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}