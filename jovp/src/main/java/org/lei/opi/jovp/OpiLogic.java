package org.lei.opi.jovp;

import org.lei.opi.core.definitions.Response;

import es.optocom.jovp.PsychoEngine;
import es.optocom.jovp.PsychoLogic;
import es.optocom.jovp.Timer;
import es.optocom.jovp.rendering.Item;
import es.optocom.jovp.rendering.Model;
import es.optocom.jovp.rendering.Texture;
import es.optocom.jovp.structures.Command;
import es.optocom.jovp.structures.ModelType;
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
  private static final double MINIMUM_TIME_FROM_ONSET = 50;

  /** The OPI driver */
  private final OpiJovp driver;

  /** Background PsychoEngine item */
  private Item[] backgrounds;
  /** Background PsychoEngine fixation target */
  private Item[] fixations;
  /** Background PsychoEngine stimulus */
  private Item stimulus;

  /** A timer to control, well, you know, er, time? */
  private Timer timer = new Timer();

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
    stimulus.hide();
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
      backgrounds[i].size(fov[0] / backgrounds.length, fov[1]);
      items.add(backgrounds[i]);
      fixations[i].position(0, 0, 98);
      items.add(fixations[i]);
    }
    stimulus.position(0, 0, 99);
    items.add(stimulus);
    driver.state = OpiJovp.State.IDLE; // State to idle and wait for instructions
  }

  /**
   * Process input
   *
   * @param command the command received
   * 
   * @since 0.0.1
   */
  @Override
  public void input(Command command) {
    if (command == Command.NONE) return;
    System.out.println(command);
    // if no response or response is too early, do nothing
    if(command != Command.YES || timer.getElapsedTime() < MINIMUM_TIME_FROM_ONSET) return;
    driver.response = new Response(true, timer.getElapsedTime(), 0.4, -0.6, 5.2, 1255);
    timer.stop();
    stimulus.hide();
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
    // Instructions are always given by the OpiDriver.
    // OpiLogic sets state back to IDLE once instruction is carried out,
    // except when presenting, where the OpiDriver waits for a response.
    switch(driver.state) {
      case IDLE -> {
        if (timer.getElapsedTime() > 0)  // do nothing unless presenting
         if (timer.getElapsedTime() > driver.stimulus.w()) { // if no response
          timer.stop();
          driver.response = new Response(false, timer.getElapsedTime(), 0.4, -0.6, 5.2, 1255);
         } else updateStimulus();
      }
      case INIT -> show(psychoEngine);
      case SETUP -> setup();
      case PRESENT -> present();
      case CLOSE -> close(psychoEngine);
    }
  }

  /** Show psychoEngine window */
  private void show(PsychoEngine psychoEngine) {
    psychoEngine.show();
    driver.state = OpiJovp.State.IDLE;
  }
  
  /** Change background */
  private void setup() {
    for (int i = 0; i < backgrounds.length; i++) {
      backgrounds[i].setColor(gammaCorrection(driver.backgrounds[i].bgCol()));
      // set new shape, position, size and rotation in fixation target
      fixations[i].update(new Model(driver.backgrounds[i].fixShape()));
      fixations[i].position(driver.backgrounds[i].fixCx(), driver.backgrounds[i].fixCy());
      fixations[i].size(driver.backgrounds[i].fixSx(), driver.backgrounds[i].fixSy());
      fixations[i].rotation(driver.backgrounds[i].fixRotation());
      // set new luminance and color in fixation target
      fixations[i].setColor(gammaCorrection(driver.backgrounds[i].fixCol()));
    }
    driver.state = OpiJovp.State.IDLE;
  }

  /** Present stimulus upon request */
  private void present() {
    updateStimulus();
    stimulus.show();
    timer.start();
    driver.state = OpiJovp.State.IDLE;
  }

  private void updateStimulus() {
    // TODO expand to allows dynamic stimuli
    stimulus.eye(driver.stimulus.eye()[0]);
    stimulus.update(new Model(driver.stimulus.shape()[0]));
    //stimulus.update(new Texture(driver.stimulus.type()[0])); // TODO: not working, revise JOVP
    stimulus.position(driver.stimulus.x()[0], driver.stimulus.y()[0]);
    stimulus.size(driver.stimulus.sx()[0], driver.stimulus.sy()[0]);
    stimulus.rotation(driver.stimulus.rotation()[0]);
    stimulus.setColor(gammaCorrection(driver.stimulus.color()[0]));
    stimulus.contrast(driver.stimulus.contrast()[0]);
    stimulus.frequency(driver.stimulus.phase()[0], driver.stimulus.frequency()[0]);
    stimulus.defocus(driver.stimulus.defocus()[0]);
    stimulus.texRotation(driver.stimulus.texRotation()[0]);
  }

  /** Apply gamma correction */
  private double[] gammaCorrection(double[] bgCol) {
    return driver.settings.calibration().colorValues(bgCol);
  }

  /** Hide psychoEngine window */
  private void close(PsychoEngine psychoEngine) {
    psychoEngine.hide();
    driver.state = OpiJovp.State.IDLE;
  }

}
