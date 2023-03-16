package org.lei.opi.jovp;

import java.util.Arrays;

import es.optocom.jovp.PsychoEngine;
import es.optocom.jovp.PsychoLogic;
import es.optocom.jovp.Timer;
import es.optocom.jovp.rendering.Item;
import es.optocom.jovp.rendering.Model;
import es.optocom.jovp.rendering.Texture;
import es.optocom.jovp.definitions.Command;
import es.optocom.jovp.definitions.ModelType;
import es.optocom.jovp.definitions.TextureType;

/**
 * Logic for the PsychoEngine
 *
 * @since 0.0.1
 */
public class OpiLogic implements PsychoLogic {

    /** {@value DEFAULT_BACKGROUND_SHAPE} */
    private static final ModelType DEFAULT_BACKGROUND_SHAPE = ModelType.CIRCLE;
    /** {@value DEFAULT_FIXATION_SHAPE} */
    private static final ModelType DEFAULT_FIXATION_SHAPE = ModelType.MALTESE;
    /** {@value DEFAULT_STIMULUS_SHAPE} */
    private static final ModelType DEFAULT_STIMULUS_SHAPE = ModelType.CIRCLE;
    /** {@value DEFAULT_FIXATION_SIZE} */
    private static final int DEFAULT_FIXATION_SIZE = 1;
    /** {@value MINIMUM_TIME_FROM_ONSET} */
    private static final double MINIMUM_TIME_FROM_ONSET = 80;

    /** The OPI driver */
    private final OpiJovp driver;

    /** Background PsychoEngine item */
    private Item[] backgrounds;
    /** Background PsychoEngine fixation target */
    private Item[] fixations;
    /** Background PsychoEngine stimulus */
    private Item stimulus;
    /** The current index into driver.getStimulus() for presentation */
    private int stimIndex = -1;

    /** A timer to control, well, you know, er, time? */
    private Timer timer = new Timer();
    /** Accumulates presentation time: useful for dynamic stimulus */
    private int presentationTime;

    OpiLogic(OpiJovp driver) {
        this.driver = driver;
        // Init background, fixation depending on whether viewMode is MONO or STEREO
        switch(driver.getConfiguration().viewMode()) {
            case MONO -> {
                backgrounds = new Item[] {
                    new Item(new Model(DEFAULT_BACKGROUND_SHAPE), new Texture())
                };
                fixations = new Item[] {
                    new Item(new Model(DEFAULT_FIXATION_SHAPE), new Texture())
                };
          }
            case STEREO -> {
                backgrounds = new Item[] {
                    new Item(new Model(DEFAULT_BACKGROUND_SHAPE), new Texture()),
                    new Item(new Model(DEFAULT_BACKGROUND_SHAPE), new Texture())
                };
                fixations = new Item[] {
                    new Item(new Model(DEFAULT_FIXATION_SHAPE), new Texture()),
                    new Item(new Model(DEFAULT_FIXATION_SHAPE), new Texture())
                };
            }
        }

        for (int i = 0; i < fixations.length; i++) 
            fixations[i].size(DEFAULT_FIXATION_SIZE);

        stimulus = new Item(new Model(DEFAULT_STIMULUS_SHAPE), new Texture());
        stimulus.show(false);
    }

    /**
     * Initialize PsychoEngine
     *
     * @param psychoEngine the psychoEngine
     * 
     * @since 0.0.1
     */
    @Override
    public void init(PsychoEngine psychoEngine) {
      // set size of the background to be the field of view
      double[] fov = psychoEngine.getFieldOfView();
      // add perimetry items: background, fixation, and stimulus.
      for (int i = 0; i < backgrounds.length; i++) {
        fixations[i].position(0, 0, driver.getConfiguration().distance() - 3);
        items.add(fixations[i]);

        backgrounds[i].position(0, 0, driver.getConfiguration().distance() - 1);
        backgrounds[i].size(fov[0], fov[1]);
        items.add(backgrounds[i]);
      }
      stimulus.position(0, 0, driver.getConfiguration().distance() - 2);
      items.add(stimulus);
      driver.setActionToNull(); // Action is over
    }

    /**
     * Process input
     *
     * @param command the command received
     * 
     * @since 0.0.1
     */
    @Override
    public void input(PsychoEngine psychoEngine, Command command) {
      if (command == Command.NONE) return;
      // if no response or response is too early, do nothing
      if(command != Command.YES || timer.getElapsedTime() < MINIMUM_TIME_FROM_ONSET) return;
      driver.setResponse(new Response(true, timer.getElapsedTime(), 0.4, -0.6, 5.2, 1255));
      timer.stop();
      stimulus.show(false);
    }

    /**
     * Update the psychoEngine input
     *
     * @param psychoEngine the psychoEngine
     * 
     * @since 0.0.1
     */
    @Override
    public void update(PsychoEngine psychoEngine) {
      double[] fov = psychoEngine.getFieldOfView();
      for (int i = 0; i < backgrounds.length; i++) 
        backgrounds[i].size(fov[0], fov[1]);

      // Instructions are always given by the OpiDriver.
      // OpiLogic sets action back to null once instruction is carried out,
      // except when presenting, where the OpiDriver waits for a response.
      //if (driver.action != null) System.out.println(driver.action);
      if (driver.getAction() == null) 
        checkAction();
      else switch(driver.getAction()) {
          case SHOW -> show(psychoEngine);
          case SETUP -> setup();
          case PRESENT -> present();
          case CLOSE -> psychoEngine.finish();
        }
    }

    /** Show psychoEngine */
    private void show(PsychoEngine psychoEngine) {
      psychoEngine.show();
      driver.setActionToNull();
    }

    /** hide psychoEngine */
    private void hide(PsychoEngine psychoEngine) {
      psychoEngine.hide();
      driver.setActionToNull();
    }

    /** Change background */
    private void setup() {
      for (int i = 0; i < backgrounds.length; i++) {
        if (driver.getBackgrounds()[i] != null) {
          double bgLum = driver.getBackgrounds()[i].bgLum();
          double[] bgCol = driver.getBackgrounds()[i].bgCol();
          double fixLum = driver.getBackgrounds()[i].fixLum();
          double[] fixCol = driver.getBackgrounds()[i].fixCol();
          backgrounds[i].setColors(gammaLumToColor(bgLum, bgCol), gammaLumToColor(bgLum, bgCol));
          fixations[i].update(new Model(driver.getBackgrounds()[i].fixShape()));
          fixations[i].position(driver.getBackgrounds()[i].fixCx(), driver.getBackgrounds()[i].fixCy());
          fixations[i].size(driver.getBackgrounds()[i].fixSx(), driver.getBackgrounds()[i].fixSy());
          fixations[i].rotation(driver.getBackgrounds()[i].fixRotation());
          fixations[i].setColor(gammaLumToColor(fixLum, fixCol));
        }
      }
      driver.setActionToNull();
    }

    /** Present stimulus upon request */
    private void present() {
      stimIndex = 0;
      updateStimulus(stimIndex);
      stimulus.show(true);
      timer.start();
      presentationTime = 0;
      driver.setActionToNull();
    }

    /** Checks if something must be updated, e.g. if presenting a stimulus or processing the observer's response */
    private void checkAction() {
        if (timer.getElapsedTime() > 0) // if timer is active, we are presenting
            if (stimulus.show() && timer.getElapsedTime() > presentationTime + driver.getStimulus(stimIndex).t()) {
                presentationTime += driver.getStimulus(stimIndex).t();
                // if presentation time is over for the last element of the array, then hide stimulus
                if (stimIndex == driver.getStimuliLength() - 1) 
                    stimulus.show(false);
                else 
                    updateStimulus(++stimIndex);
            } else if (timer.getElapsedTime() > driver.getStimulus(stimIndex).w()) {
                // if no response, reset timer and send negative response
                timer.stop();
                    // TODO tracking results need to be put in response
                driver.setResponse(new Response(false, timer.getElapsedTime(), 0, 0, 0, 0));
            };
    }

    /** Update stimulus upon request */
    private void updateStimulus(int index) {
        Stimulus stim = driver.getStimulus(index);
        stimulus.eye(stim.eye());
            // for performance, do not regenerate stimulus model and texture unless it has changed
        boolean newTexture = index == 0;
        boolean newModel = index == 0;
        if (index > 0) {
            Stimulus prevStim = driver.getStimulus(index - 1);
            if(stim.shape() != prevStim.shape()
            || (stim.shape() == ModelType.OPTOTYPE && prevStim.shape() == ModelType.OPTOTYPE && !stim.optotype().equals(prevStim.optotype())))
                newModel = true;

            if ((stim.type() != prevStim.type())
            || (stim.type() == TextureType.IMAGE && prevStim.type() == TextureType.IMAGE && !stim.imageFilename().equals(prevStim.imageFilename())))
                newTexture = true;
        }

        if (newModel)
            if (stim.shape() == ModelType.OPTOTYPE)
                stimulus.update(new Model(stim.optotype()));  // give it the optotype
            else
                stimulus.update(new Model(stim.shape()));
        if (newTexture)
            if (stim.type() == TextureType.IMAGE)
                stimulus.update(new Texture(stim.imageFilename()));  // give it the string filename
            else
                stimulus.update(new Texture(stim.type()));  

        stimulus.position(stim.x(), stim.y());
        stimulus.size(stim.sx(), stim.sy());
        stimulus.rotation(stim.rotation());
          // NOTE - JOVP seems to have color1 and color2 reversed for FLAT
        stimulus.setColors(gammaLumToColor(stim.lum(), stim.colorMin()), gammaLumToColor(stim.lum(), stim.colorMax()));
        stimulus.contrast(stim.contrast());
        stimulus.frequency(stim.phase(), stim.frequency());
        stimulus.defocus(stim.defocus());
        stimulus.texRotation(stim.texRotation());
    }

    /** 
     * Apply inverse gamma to convert cd/m^2 to RGB in [0, 1]
     * @param luminance [0]=R [1]=G [2]=B cd/m^2
     * */
    private double[] gammaLumToColor(double[] luminance) {
      return driver.getConfiguration().calibration().getColorValues(luminance);
    }

      // multiply luminance * color and then convert 
    private double[] gammaLumToColor(double luminance, double[] color) {
      double lum[] = Arrays.stream(color).map((double c) -> c * luminance).toArray();
      return gammaLumToColor(lum);
    }

}
