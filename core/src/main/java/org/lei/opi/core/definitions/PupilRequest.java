package org.lei.opi.core.definitions;

import es.optocom.jovp.definitions.ViewEye;

/** The data holding a request to the camera for a pupil position */
public class PupilRequest {
    long timeStamp;           // some timestamp of the request (used to match responses, perhaps)
    ViewEye eye;              // eye for which to get the response
    int numberOfTries;        // The number of times this request has been attempted to be completed

    /** The maximum number of times/frames to try and find pupil to satisfy request */
    static final int MAX_TRIES_FOR_REQUEST = 10;
    /** The maximum number of milliseconds to allow a frame to answer a request (should be a 2^x + 1) */
    public static final int MAX_TIME_DIFFERENCE_TO_SATISFY_REQUEST = 513;

    public PupilRequest(long timeStamp, ViewEye eye) {
        this.timeStamp = timeStamp;
        this.eye = eye;
        this.numberOfTries = 0;
    }

    public ViewEye eye() { return eye; }
    public long timeStamp() { return timeStamp; }

    /*
    * @return true If we can increment the number of tries, false if we have hit the limit.
    * SIDE EFFECT - increment the number of tries
    */
    public boolean incTries() { 
        this.numberOfTries++;
        return this.numberOfTries < MAX_TRIES_FOR_REQUEST;
    }

    /*
    * @param otherTimeStamp A time stamp to which to compare this.timeStamp
    * @param tol The tolerance for the comparison
    * @return true if the difference between this.timeStamp and otherTimeStamp is less than tol, false otherwise
    */
    public boolean closeEnough(long otherTimeStamp, int tol) {
        return Math.abs(this.timeStamp - otherTimeStamp) < tol;
    }
}