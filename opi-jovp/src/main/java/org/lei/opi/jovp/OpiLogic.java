package org.lei.opi.jovp;

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

    /** Default background shape */
    private static final ModelType DEFAULT_BACKGROUND_SHAPE = ModelType.CIRCLE;
    /** Default background shape */
    private static final ModelType DEFAULT_FIXATION_SHAPE = ModelType.MALTESE;
    /** Default stimulus shape */
    private static final ModelType DEFAULT_STIMULUS_SHAPE = ModelType.CIRCLE;
    /** Default fixation size */
    private static final int DEFAULT_FIXATION_SIZE = 1;
    /** Minimum time from stimulus onset */
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
    Timer timer = new Timer();
    /** time from onset to keep track of the presentation */
    long ellapseTime = -1;
    /** Whether stimulus is displaying */
    boolean onset = false;
    /** Whether observer responded to stimulus */
    boolean gotAnswer = false;

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
      // add perimetry items, stimulus, fixation, background. Order matters
      for (int i = 0; i < backgrounds.length; i++) {
        items.add(fixations[i]);
      }
      items.add(stimulus);
      for (int i = 0; i < backgrounds.length; i++) {
        backgrounds[i].size(fov[0] / backgrounds.length, fov[1]);
        items.add(fixations[i]);
        items.add(backgrounds[i]);
      }
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
      gotAnswer = true;
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
      if (driver.initialized) psychoEngine.show();
      else psychoEngine.hide();
      // check if background and fixation upon request
      setBackgroundAndFixation();
      // present stimulus
      presentStimulus();
    }

    /** Change background */
    private void setBackgroundAndFixation() {
      for (int i = 0; i < backgrounds.length; i++) {
        // check if background and fixation settings have changed
        if(driver.backgrounds[i] != null) {
          backgrounds[i].setColor(driver.backgrounds[i].bgCol());
          // TODO: allow to change fixation shape
          // set new position, size and rotation in fixation target
          fixations[i].position(driver.backgrounds[i].fixCx(), driver.backgrounds[i].fixCy());
          fixations[i].size(driver.backgrounds[i].fixSx(), driver.backgrounds[i].fixSy());
          fixations[i].rotation(driver.backgrounds[i].fixRotation());
          // set new luminance and color in fixation target
          fixations[i].setColor(driver.backgrounds[i].fixCol());
          driver.backgrounds[i] = null; // set background record to null when done with it
        }
      }
    }

    /** Present stimulus upon request*/
    private void presentStimulus() {
      // onset: show stimulus and start timer
      if (!onset && driver.stimulus != null) {
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
        onset = true;
      }
      if(!onset) return;
    }

  }
