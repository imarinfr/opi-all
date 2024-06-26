package org.lei.opi.core.definitions;

/**
/* Hold and manipulate info and about frame 
 *
 * @author Andrew Turpin
 * @date 5 June 2024 
 */
public class FrameInfoNone extends FrameInfo {
    public FrameInfoNone() {
        super();
    }

    public void findPupil() {
        this.hasPupil = false;
        return;
    }
}