package org.lei.opi.core;

public abstract class MessageProcessor {

    public static class Packet {
        public boolean close;
        public String msg;
        public Packet(boolean close, String msg) { this.close = close ; this.msg = msg; }
        public Packet(String msg) { this.close = false ; this.msg = msg; }
    }

    /** 
    * Process incoming strings from the socket.
    * 
    * @param incomingString
    * @return String reply to send back on socket, or null to close socket.
    */
    public abstract Packet process(String incomingString);
}
