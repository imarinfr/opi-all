package org.lei.opi.jovp;

import static org.lei.opi.jovp.JsonProcessor.toColorArray;
import static org.lei.opi.jovp.JsonProcessor.toDoubleArray;
import static org.lei.opi.jovp.JsonProcessor.toStringArray;
import static org.lei.opi.jovp.JsonProcessor.toObjectStream;

import java.util.HashMap;

import es.optocom.jovp.definitions.EnvelopeType;
import es.optocom.jovp.definitions.Eye;
import es.optocom.jovp.definitions.ModelType;
import es.optocom.jovp.definitions.Optotype;
import es.optocom.jovp.definitions.TextureType;

/**
* A record to hold information about one Stimulus
* 
* @param eye eye where to present the stimulus
* @param shape stimulus shape
* @param type stimulus type
* @param x x center of the stimulus in degrees of visual angle
* @param y y center of the stimulus in degrees of visual angle
* @param sx major axis size of the stimulus in degrees of visual angle
* @param sy minor axis size of the stimulus in degrees of visual angle
* @param lum cd/m^2 for stimulus 
* @param color1 stimulus color 1 for flat surfaces and patterns
* @param color2 stimulus color 2 for patterns
* @param rotation rotation of the stimulus in degrees
* @param contrast stimulus contrast
* @param phase stimulus spatial phase
* @param frequency stimulus spatial frequency
* @param defocus stimulus defocus in Diopters for stimulus such as "Gaussian blob", Gabors, etc
* @param texRotation stimulus pattern rotation in degrees
* @param t presentation time in ms
* @param w response window in ms
* @param imageFilename If type == IMAGE, the filename of the image to present
* @param fullFoV If !0 then ignore sx and sy and scale the stimulus to the full field of view, otherwise use sx and sy to scale the stim.
* @param optotype If shape == OPTOTYPE, the letter A to Z of the optotype to present
* @param envType Type of envelope
* @param envSdx Standard deviation in x for envelope (ignored if envType == NONE)
* @param envSdy Standard deviation in y for envelope (ignored if envType == NONE)
* @param envRotation Rotation of envelope (ignored if envType == NONE)
*
* @since 0.0.1
*/
public record Stimulus(Eye eye, ModelType shape, TextureType type,
                      double x, double y, double sx, double sy,
                      double lum, 
                      double[] color1, 
                      double[] color2, 
                      double rotation, 
                      double contrast,
                      double phase, double frequency, double defocus,
                      double texRotation, 
                      double t, double w,
                      String imageFilename,
                      double fullFoV,
                      Optotype optotype,
                      EnvelopeType envType, 
                      double envSdx, double envSdy, double envRotation) {

    /**
     * Create an array of stimulus record from R OPI of length `stim.length`
     * 
     * @param args pairs of argument name and value
     * 
     * @return a stimulus record
     * 
     * @throws ClassCastException Cast exception
     * @throws IllegalArgumentException If any value is bad
     * @throws SecurityException
     * @throws NoSuchMethodException
     * 
     * @since 0.0.1
     */
    public static Stimulus[] create(HashMap<String, Object> args) throws ClassCastException, IllegalArgumentException, NoSuchMethodException, SecurityException {
        int n = Number.class.cast((Double)args.get("stim.length")).intValue() ;
        Stimulus[] stimuli = new Stimulus[n];

        for (int index = 0 ; index < n ; index++) {
            stimuli[index] = new Stimulus(
                toObjectStream(args.get("eye"), Eye.class).toArray(Eye[]::new)[index],
                toObjectStream(args.get("shape"), ModelType.class).toArray(ModelType[]::new)[index],
                toObjectStream(args.get("type"), TextureType.class).toArray(TextureType[]::new)[index],
                toDoubleArray(args.get("x"))[index], 
                toDoubleArray(args.get("y"))[index],
                toDoubleArray(args.get("sx"))[index], 
                toDoubleArray(args.get("sy"))[index],
                toDoubleArray(args.get("lum"))[index], 
                toColorArray(args.get("color1"))[index], 
                toColorArray(args.get("color2"))[index],
                toDoubleArray(args.get("rotation"))[index], 
                toDoubleArray(args.get("contrast"))[index],
                toDoubleArray(args.get("phase"))[index], 
                toDoubleArray(args.get("frequency"))[index],
                toDoubleArray(args.get("defocus"))[index], 
                toDoubleArray(args.get("texRotation"))[index],
                toDoubleArray(args.get("t"))[index], 
                (double)args.get("w"),
                toStringArray(args.get("imageFilename"))[index],
                toDoubleArray(args.get("fullFoV"))[index],
                toObjectStream(args.get("optotype"), Optotype.class).toArray(Optotype[]::new)[index],
                toObjectStream(args.get("envType"), EnvelopeType.class).toArray(EnvelopeType[]::new)[index],
                toDoubleArray(args.get("envSdx"))[index],
                toDoubleArray(args.get("envSdy"))[index],
                toDoubleArray(args.get("envRotation"))[index]
            );
        }
        return stimuli;
    }
} 