package org.lei.opi.monitor;

import javafx.scene.control.TextArea;
import java.io.Writer;

public class MessageWriter extends Writer {
    TextArea ta;

    MessageWriter(TextArea ta) { this.ta = ta; }
    public void close() { }
    public void flush() { }
    
    /**
     * Append string in s[off : off+len] to TextArea this.ta
     */
    public void write(char[] s, int off, int len) { ta.appendText(new String(s, off, len)); }
}
