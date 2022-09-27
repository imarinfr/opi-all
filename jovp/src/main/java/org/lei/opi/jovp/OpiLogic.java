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
  private static final int MINIMUM_TIME_FROM_ONSET = 50;

  /** The OPI driver */
  private final OpiDriver driver;

  /** Background PsychoEngine item */
  private Item[] backgrounds;
  /** Background PsychoEngine fixation target */
  private Item[] fixations;
  /** Background PsychoEngine stimulus */
  private Item stimulus;

  /** A timer to control, well, you know, er, time? */
  private Timer timer = new Timer();
  /** time from onset to keep track of the presentation */
  private long ellapseTime = -1;

  OpiLogic(OpiDriver driver) {
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
    driver.state = OpiDriver.State.IDLE; // State to idle and wait for instructions
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
    if(command != Command.YES || ellapseTime < MINIMUM_TIME_FROM_ONSET) return;
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
      case IDLE -> {} //Do nothing
      case INIT -> initialize(psychoEngine);
      case SETUP -> setup();
      case PRESENT -> present();
      case WAIT -> waitForResponse();
      case RESPONDED -> respond();
      case CLOSE -> close(psychoEngine);
    }
  }

  /** Show psychoEngine window */
  private void initialize(PsychoEngine psychoEngine) {
    psychoEngine.show();
    driver.state = OpiDriver.State.IDLE;
  }

  /** Hide psychoEngine window */
  private void close(PsychoEngine psychoEngine) {
    psychoEngine.hide();
    driver.state = OpiDriver.State.IDLE;
  }
  
  /** Change background */
  private void setup() {
    for (int i = 0; i < backgrounds.length; i++) {
      backgrounds[i].setColor(driver.backgrounds[i].bgCol());
      // set new shape, position, size and rotation in fixation target
      fixations[i].update(new Model(driver.backgrounds[i].fixShape()));
      fixations[i].position(driver.backgrounds[i].fixCx(), driver.backgrounds[i].fixCy());
      fixations[i].size(driver.backgrounds[i].fixSx(), driver.backgrounds[i].fixSy());
      fixations[i].rotation(driver.backgrounds[i].fixRotation());
      // set new luminance and color in fixation target
      fixations[i].setColor(driver.backgrounds[i].fixCol());
    }
    driver.state = OpiDriver.State.IDLE;
  }

  /** Present stimulus upon request */
  private void present() {
    // TODO expand to allows dynamic stimuli
    stimulus.update(new Model(driver.stimulus.shape()[0]));
    stimulus.position(driver.stimulus.x()[0], driver.stimulus.y()[0]);
    stimulus.size(driver.stimulus.sx()[0], driver.stimulus.sy()[0]);
    stimulus.rotation(driver.stimulus.rotation()[0]);
    stimulus.setColor(driver.stimulus.color()[0]);
    stimulus.contrast(driver.stimulus.contrast()[0]);
    stimulus.frequency(driver.stimulus.phase()[0], driver.stimulus.frequency()[0]);
    stimulus.defocus(driver.stimulus.defocus()[0]);
    stimulus.texRotation(driver.stimulus.texRotation()[0]);
    stimulus.show();
    timer.start();
    driver.state = OpiDriver.State.WAIT;
  }

  /** Wait for obersver's response */
  private void waitForResponse() {
    if(timer.getElapsedTime() >= 1500) {
      System.out.println(timer.getElapsedTime());
      stimulus.hide();
      driver.state = OpiDriver.State.RESPONDED;
    }
  }

  /** Send response from stimulus presentation */
  private void respond() {
    // TODO: build response
    driver.response = new Response(true, driver.prefix + OpiDriver.PRESENT_ERROR, false, 1234, -1, -1, -1, -1);
    driver.state = OpiDriver.State.IDLE;
  }

}
