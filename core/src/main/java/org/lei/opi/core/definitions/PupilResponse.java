package org.lei.opi.core.definitions;

/** Data returned in response to a request for a pupil position */
public record PupilResponse(
    long requestTimeStamp,       // timestamp of request object that initiated this response
    long acquisitionTimeStamp,   // timestamp of frame acquisition (approximate)
    int x,                    // pupil position with (0,0) at centre of image (degrees)
    int y,                    // pupil position with (0,0) at centre of image (degrees)
    int diameter              // pupil diameter in mm (-1 indicates no pupil found)
) {
    public PupilResponse(long requestTimeStamp, long acquisitionTimeStamp) {
        this(requestTimeStamp, acquisitionTimeStamp, -1, -1, -1);
    }

    public PupilResponse set(int x, int y, int diameter) {
        return new PupilResponse(this.requestTimeStamp, this.acquisitionTimeStamp, x, y, diameter);
    }

    public boolean pupilFound() { return diameter > 0; }
}