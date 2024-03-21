package org.lei.opi.core.definitions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.lei.opi.core.OpiListener;
import org.lei.opi.core.OpiMachine;

/**
 * A class to hold string messages with attributes attached.
 */
public class Packet {
    /** true if the socket should be closed that receives this message */
    private boolean close;
    /** true if this message packet contains an error msg */
    private boolean error;
    /** A valid JSON string */
    private String msg;

    public Packet(boolean error, boolean close, Object o) { 
        this.error = error; 
        this.close = close; 
        this.msg = OpiListener.gson.toJson(o);
    }

    public Packet(Object obj) { this(false, false, obj);}
    public Packet(boolean close, Object obj) { this(false, close, obj);}

    public boolean getClose() { return this.close; }
    public boolean getError() { return this.error; }
    public String  getMsg() { return this.msg; }

    public String toString() { return String.format("Packet\n\tError: %s\n\tClose: %s\n\tMsg: %s\n", error, close, getMsg()); }

    /**
     * Create a Packet with error=true
     * 
     * @param description String error description (no extra quotes - so not JSON)
     * 
     * @return Packet 
     * 
     * @since 0.2.0
     */
    public static Packet error(String description) { return new Packet(true, false, description); }
   
    /**
     * Create a Packet with error=true that includes both a description and an exception.
     * 
     * @param description String error description (no quotes) to add to Json return name 'description'
     * @param exception An exception to print to stderr and add to Json return object name 'exception'
     * 
     * @return Packet
     * 
     * @since 0.1.0
     */
    public static Packet error(String description, Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String s = sw.toString(); // stack trace as a string
        pw.close();

        return error(description + "\n\n" + s);
    }


    /**
     * Create a Packet containing msg if it contains all of the ReturnMsg keys 
     * for `commandName` of `machineClass`.
     *
     * @param msg A valid JSON string 
     * @param methods The opiMethods hashmap from the OpiMachine class that is creating the packet
     *
     * @return New packet that either has error=false and contains msg, or 
     *         error-true and msg has missing ReturnMsgs.
     */
    public static Packet checkReturnElements(String msg, HashMap<String, OpiMachine.MethodData> methods, String commandName) {
        assert(methods.containsKey(commandName));
        HashSet<ReturnMsg> rms = methods.get(commandName).returnMsgs();

        JsonElement je = OpiListener.gson.fromJson(msg, JsonElement.class);

        if (je.isJsonPrimitive() && rms.size() == 0) {
            return new Packet(false, false, msg);
        }

        assert(je.isJsonObject());
        JsonObject jo = je.getAsJsonObject();
        for (ReturnMsg rm : rms) {
            if (!jo.has(rm.name()))
                return new Packet(true, false, 
                    "Missing return field: " + rm.name() + " for command " + commandName + " in " + msg.getClass().getName());
        }
        return new Packet(false, false, msg);
    }

    public static Packet checkReturnElements(Object obj, HashMap<String, OpiMachine.MethodData> methods, String commandName) {
        return checkReturnElements(OpiListener.gson.toJson(obj), methods, commandName);
    }

    /**
     * Create if Packet.msg contains all of the ReturnMsg keys 
     * for `commandName` of `machineClass`.
     *
     * @param packet A packet to check
     * @param methods The opiMethods hashmap from the OpiMachine class that is creating the packet
     *
     * @return New packet that either has error=false and contains msg, or 
     *         error-true and msg has missing ReturnMsgs.
     */
    public static Packet checkReturnElements(Packet packet, HashMap<String, OpiMachine.MethodData> methods, String commandName) {
        if (packet.getError())
            return packet;

        Packet p = checkReturnElements(packet.getMsg(), methods, commandName);
        if (!p.getError())
            return packet;
        else 
            return p;
    }
}