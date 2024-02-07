package org.lei.opi.jovp;

//import org.lwjgl.glfw.GLFWVidMode;  see TODO below

import es.optocom.jovp.Monitor;
import es.optocom.jovp.definitions.ViewMode;

/**
 * Device settings to return upon query from OPI monitor
 *
 * @param distance viewing distance
 * @param fov current horizontal and vertical field of view in degrees of visual angle
 * @param viewMode viewing mode: MONO or STEREO
 * @param input Either 'mouse', 'keypad', or the name of a suitable USB controller
 * @param pseudoGray whether JOVP is using a bit-stealing algorithm
 * @param fullScreen whether JOVP machine is running on full screen mode
 * @param tracking  whether JOVP machine is able to do eye tracking
 * @param maxLum maximum luminance on the R, G, and B channels (cd/m^2)
 * @param gammaFile the path of the gamma file
 * @param monitor display of the OPI JOVP machine
 * @since 0.0.1
 */
public record Query(int distance, float[] fov, ViewMode viewMode, String input, boolean pseudoGray,
                    boolean fullScreen, boolean tracking, double[] maxLum, String gammaFile, Monitor monitor) {

}