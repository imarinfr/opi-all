package org.lei.opi.core;

/**
 * A class for managers/drivers to extend so that they have a process() method that will 
 * deal with incoming string messages in some way and return a Packet.
 *
 * @since 0.0.1
 */
public abstract class MessageProcessor {

    /**
     * A class to hold string messages with attributes attached.
     */
    public static class Packet {
        /** true if the socket should be closed that receives this message */
        public boolean close;
        /** true if this message packet contains an error msg */
        public boolean error;
        /** Some sort of message - probably Json, but maybe not. */
        public String msg;

        /** Simple constrcutor 0 */
        public Packet() { this.close = false ; this.msg = ""; this.error = false; }
        /** Simple constrcutor 1 */
        public Packet(String msg) { this.close = false ; this.msg = msg; this.error = false; }
        /** Simple constructor 2 */
        public Packet(boolean close, String msg) { this.close = close ; this.msg = msg; this.error = false;}
        /** Simple constructor 3 */
        public Packet(boolean error, boolean close, String msg) { this.close = close ; this.msg = msg; this.error = error;}
    }

    /** 
    * Process incoming strings from the socket.
    * 
    * @param incomingString
    * @return String reply to send back on socket, or null to close socket.
    */
    public abstract Packet process(String incomingString);
}
