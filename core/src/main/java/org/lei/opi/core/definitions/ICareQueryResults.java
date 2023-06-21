package org.lei.opi.core.definitions;

public record ICareQueryResults(
    float prlx,
    float prly,
    float onhx,
    float onhy,
    String image,
    double minX,
    double maxX,
    double minY,
    double maxY,
    double minPresentationTime,
    double maxPresentationTime,
    double minResponseWindow,
    double maxResponseWindow,
    double backgroundLuminance,
    double minLuminance,
    double maxLuminance,
    boolean tracking) 
{ 
    public String toString() {
        return String.format("""
            prlx: %s
            prly: %s
            onhx: %s
            onhy: %s
            image: %s
            minX: %s
            maxX: %s
            minY: %s
            maxY: %s
            minPresentationTime: %s
            maxPresentationTime: %s
            minResponseWindow: %s
            maxResponseWindow: %s
            backgroundLuminance: %s
            minLuminance: %s
            maxLuminance: %s
            tracking: %s\n""", 
            prlx,
            prly,
            onhx,                    
            onhy,
            image,
            minX,
            maxX,
            minY,
            maxY,
            minPresentationTime,
            maxPresentationTime,
            minResponseWindow,
            maxResponseWindow,
            backgroundLuminance,
            minLuminance,
            maxLuminance,
            tracking
            );
    }
}