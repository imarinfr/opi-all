package org.lei.opi.jovp;

import org.lei.opi.core.definitions.Response;

import es.optocom.jovp.PsychoEngine;
import es.optocom.jovp.PsychoLogic;
import es.optocom.jovp.Timer;
import es.optocom.jovp.rendering.Item;
import es.optocom.jovp.rendering.Model;
import es.optocom.jovp.rendering.Texture;
import es.optocom.jovp.definitions.Command;
import es.optocom.jovp.definitions.ModelType;
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
  /** Array index for dynamic stimuli presentation */
  private int index = -1;

  /** A timer to control, well, you know, er, time? */
  private Timer timer = new Timer();
  /** Accumulates presentation time: useful for dynamic stimulus */
  int presentationType;

  OpiLogic(OpiJovp driver) {
    this.driver = driver;
    // Init background, fixation depending on whether viewMode is MONO or STEREO
    switch(driver.settings.viewMode()) {
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
    // set size of the backgroud to be the field of view
    for (int i = 0; i < fixations.length; i++) fixations[i].size(DEFAULT_FIXATION_SIZE);
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
    // set size of the backgroud to be the field of view
    double[] fov = psychoEngine.getFieldOfView();
    // add perimetry items: background, fixation, and stimulus.
    for (int i = 0; i < backgrounds.length; i++) {
      backgrounds[i].position(0, 0, 100);
      backgrounds[i].size(fov[0], fov[1]);
      items.add(backgrounds[i]);
      fixations[i].position(0, 0, 98);
      items.add(fixations[i]);
    }
    stimulus.position(0, 0, 99);
    items.add(stimulus);
    driver.state = null; // State to idle and wait for instructions
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
    driver.response = new Response(true, timer.getElapsedTime(), 0.4, -0.6, 5.2, 1255);
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
    for (int i = 0; i < backgrounds.length; i++) backgrounds[i].size(fov[0], fov[1]);
    // Instructions are always given by the OpiDriver.
    // OpiLogic sets state back to IDLE once instruction is carried out,
    // except when presenting, where the OpiDriver waits for a response.
    if (driver.state != null) System.out.println(driver.state);
    if (driver.state == null) checkState();
    else switch(driver.state) {
        case INIT -> show(psychoEngine);
        case SETUP -> setup();
        case PRESENT -> present();
        case CLOSE -> hide(psychoEngine);
      }
  }

  /** Show psychoEngine */
  private void show(PsychoEngine psychoEngine) {
    psychoEngine.show();
    driver.state = null;
  }

  /** Show psychoEngine */
  private void hide(PsychoEngine psychoEngine) {
    psychoEngine.hide();
    driver.state = null;
  }

  /** Change background */
  private void setup() {
    for (int i = 0; i < backgrounds.length; i++) {
      backgrounds[i].setColor(gammaCorrection(driver.backgrounds[i].bgCol()));
      fixations[i].update(new Model(driver.backgrounds[i].fixShape()));
      fixations[i].position(driver.backgrounds[i].fixCx(), driver.backgrounds[i].fixCy());
      fixations[i].size(driver.backgrounds[i].fixSx(), driver.backgrounds[i].fixSy());
      fixations[i].rotation(driver.backgrounds[i].fixRotation());
      fixations[i].setColor(gammaCorrection(driver.backgrounds[i].fixCol()));
    }
    driver.state = null;
  }

  /** Present stimulus upon request */
  private void present() {
    index = 0;
    updateStimulus(index);
    stimulus.show(true);
    timer.start();
    presentationType = 0;
    driver.state = null;
  }

  /** Checks if something must be updated, e.g. if presenting a stimulus or processing the observer's response */
  private void checkState() {
    if (timer.getElapsedTime() > 0)// if not presenting do nothing
      if (stimulus.show() && timer.getElapsedTime() > presentationType + driver.stimulus.t()[index]) {
        presentationType += driver.stimulus.t()[index];
        // if presentation time is over for the last element of the array, then hide stimulus
        if (index == driver.stimulus.length() - 1) stimulus.show(false);
        else updateStimulus(++index);
      } else if (timer.getElapsedTime() > driver.stimulus.w()) {
        // if no response, reset timer and send negative response
        timer.stop();
        driver.response = new Response(false, timer.getElapsedTime(), 0.4, -0.6, 5.2, 1255);
      };
  }

  /** Update stimulus upon request */
  private void updateStimulus(int index) {
    stimulus.eye(driver.stimulus.eye()[index]);
    if (index > 0) {
      // for performance, do not regenerate stimulus model and texture unless it has changed
      if(driver.stimulus.shape()[index] != driver.stimulus.shape()[index - 1])
        stimulus.update(new Model(driver.stimulus.shape()[index]));
      if(driver.stimulus.type()[index] != driver.stimulus.type()[index - 1])
        stimulus.update(new Texture(driver.stimulus.type()[index]));
    } else stimulus.update(new Model(driver.stimulus.shape()[index]), new Texture(driver.stimulus.type()[index]));
    stimulus.position(driver.stimulus.x()[index], driver.stimulus.y()[index]);
    stimulus.size(driver.stimulus.sx()[index], driver.stimulus.sy()[index]);
    stimulus.rotation(driver.stimulus.rotation()[index]);
    stimulus.setColors(gammaCorrection(driver.stimulus.color1()[index]), gammaCorrection(driver.stimulus.color2()[index]));
    stimulus.contrast(driver.stimulus.contrast()[index]);
    stimulus.frequency(driver.stimulus.phase()[index], driver.stimulus.frequency()[index]);
    stimulus.defocus(driver.stimulus.defocus()[index]);
    stimulus.texRotation(driver.stimulus.texRotation()[index]);
  }

  /** Apply gamma correction */
  private double[] gammaCorrection(double[] bgCol) {
    return driver.settings.calibration().colorValues(bgCol);
  }

}
