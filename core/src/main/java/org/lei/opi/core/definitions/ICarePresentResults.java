package org.lei.opi.core.definitions;

public record ICarePresentResults (
    boolean seen,
    int time,       // response time (ms)
    int eyex,       // location of projection on image in pixels
    int eyey,       // location of projection on image in pixels
    float eyed,
    int eyet,    // hardware time of button press
    long time_rec,  // epoch time command received
    long time_resp,  // epoch time command response
    int num_track_events,
    int num_motor_fails) 
{ 

    public String toString() {
        return String.format("""
        seen: %s
        time: %s
        eyex: %s
        eyey: %s
        eyed: %s
        eyet: %s
        time_rec: %s
        time_resp: %s
        num_track_events: %s
        num_motor_fails: %s\n""",
        seen,
        time,       // response time (ms)
        eyex,       // location of projection on image in pixels
        eyey,       // location of projection on image in pixels
        eyed,
        eyet,    // hardware time of button press
        time_rec,  // epoch time command received
        time_resp,  // epoch time command response
        num_track_events,
        num_motor_fails);
    }
}
