package org.lei.opi.core.definitions;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.lei.opi.core.OpiListener;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * A class to hold string messages with attributes attached.
 */
public class Packet {
    /** {@value JSON_TRUE} */
    private static final String JSON_TRUE = "true";
    /** {@value JSON_FALSE} */
    private static final String JSON_FALSE = "false";

    /** Constant for exception messages: {@value BAD_JSON} */
    public static final String BAD_JSON = "String is not a valid Json object.";

    /** true if the socket should be closed that receives this message */
    private boolean close;
    /** true if this message packet contains an error msg */
    private boolean error;
    /** Either a String or a JSON Object */
    private Object msg;
    /** The type of the msg which might be needed if want to fromJson to this*/
    private Class<?> type;

    public Packet(boolean error, boolean close, Object msg, Class<?> type) { this.close = close ; this.msg = msg; this.error = error; this.type = type;}

    public Packet() { this(false, false, "", String.class);}
    public Packet(String s) { this(false, false, (Object)s, String.class);}
    public Packet(Object p) { this(false, false, p, p.getClass());}
    public Packet(boolean close, String str) { this(false, close, (Object)str, String.class);}
    public Packet(boolean error, boolean close, String str) {this(error, close, (Object)str, String.class);} 

    public boolean  getClose() { return this.close; }
    public boolean  getError() { return this.error; }
    public Class<?> getType()  { return this.type; }
    public Object   getMsg() throws ClassCastException { return this.type.cast(this.msg); }

    public String toString() { return String.format("Packet\n\tError: %s\n\tClose: %s\n\tMsg: %s\n", error, close, getMsg()); }
    public String toJson() { 
        return String.format("{\"error\":%s,\"close\":%s,\"msg\":%s}", 
            this.error ? JSON_TRUE : JSON_FALSE, 
            this.close ? JSON_TRUE : JSON_FALSE, 
            OpiListener.gson.toJson(this.getMsg()));     // removed 19 June 2023 - will it break JOVP?
    }

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
        pw.close();

        record Err(String description, String exception) { };
        Err err = new Err(description, pw.toString());
        try {
            return error(OpiListener.gson.toJson(err));
        } catch (JsonSyntaxException e) {
            return error("Unknown error");
        }
    }
}