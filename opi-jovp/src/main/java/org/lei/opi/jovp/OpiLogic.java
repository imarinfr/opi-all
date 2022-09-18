package org.lei.opi.jovp;

import es.optocom.jovp.PsychoEngine;
import es.optocom.jovp.PsychoLogic;
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
  public class OpiLogic implements PsychoLogic{

    /** Default background shape */
    private static final ModelType DEFAULT_BACKGROUND_SHAPE = ModelType.CIRCLE;
    /** Default background shape */
    private static final ModelType DEFAULT_FIXATION_SHAPE = ModelType.MALTESE;
    /** Default stimulus shape */
    private static final ModelType DEFAULT_STIMULUS_SHAPE = ModelType.CIRCLE;
    /** Default fixation color */
    private static final double[] DEFAULT_FIXATION_COLOR = new double[] {0, 0.25, 0, 1};
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
    int ellapseTime = 0;
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
            new Item(new Model(DEFAULT_FIXATION_SHAPE), new Texture(DEFAULT_FIXATION_COLOR))
          };
        }
        case STEREO -> {
          backgrounds = new Item[] {
            new Item(new Model(DEFAULT_BACKGROUND_SHAPE), new Texture()),
            new Item(new Model(DEFAULT_BACKGROUND_SHAPE), new Texture())
          };
          fixations = new Item[] {
            new Item(new Model(DEFAULT_FIXATION_SHAPE), new Texture(DEFAULT_FIXATION_COLOR)),
            new Item(new Model(DEFAULT_FIXATION_SHAPE), new Texture(DEFAULT_FIXATION_COLOR))
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
      for (int i = 0; i < backgrounds.length; i++) backgrounds[i].size(fov[0], fov[1]);
      // add perimetry items, background, fixation, stimulus
      items.add(backgrounds[0]);
      items.add(fixations[0]);
      items.add(stimulus);
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
      if (driver.initialized) {
        psychoEngine.show();
      } else psychoEngine.hide();
      for (int i = 0; i < backgrounds.length; i++) {
        // check if background and fixation settings have changed
        if(driver.backgrounds[i] != null) {
          setBackground();
          driver.backgrounds[i] = null; // set background record to null when done with it
        }
      }
    }
 
    /** Change background */
    private void setBackground() {
      // set new luminance and color in background
      driver.backgrounds[0].bgLum();
      driver.backgrounds[0].bgCol();
      backgrounds[0].setColor(driver.backgrounds[0].bgCol());
      // TODO: fixation setType
      // set new position, size and rotation in fixation target
      fixations[0].position(driver.backgrounds[0].fixCx(), driver.backgrounds[0].fixCy());
      fixations[0].size(driver.backgrounds[0].fixSx(), driver.backgrounds[0].fixSy());
      fixations[0].rotation(driver.backgrounds[0].fixRotation());
      // set new luminance and color in fixation target
      driver.backgrounds[0].fixLum();
      driver.backgrounds[0].fixCol();
      fixations[0].setColor(driver.backgrounds[0].fixCol());
    }

    /** Present stimulus */
    private void present() {
      // once done, set stimulus record to null
      driver.stimulus = null;
    }

  }
