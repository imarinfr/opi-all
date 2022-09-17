package org.lei.opi.jovp;

import es.optocom.jovp.PsychoEngine;
import es.optocom.jovp.PsychoLogic;
import es.optocom.jovp.Timer;
import es.optocom.jovp.rendering.Item;
import es.optocom.jovp.rendering.Model;
import es.optocom.jovp.rendering.Texture;
import es.optocom.jovp.structures.Command;
import es.optocom.jovp.structures.Eye;
import es.optocom.jovp.structures.ModelType;

/**
 * The OPI JOVP logic
 *
 * @since 0.0.1
 */
public class OpiLogic implements PsychoLogic {

  /** Minimum time from stimulus onset */
  private static final double[] BLACK = new double [] {0, 0, 0, 0};
  private static final int MINIMUM_TIME_FROM_ONSET = 50;

  /** A background record to communicate with JOVP */
  private Background bgRecord;
  /** A stimulus record to communicate with JOVP */
  private Stimulus stRecord;
  /** A timer to control, well, you know, er, time? */
  private Timer timer;

  private final Item background;
  private final Item fixation;
  private final Item stimulus;

  /** A timer to control, well, you know, er, time? */
  int ellapseTime = 0;
  /** Whether stimulus is displaying */
  boolean onset = false;
  /** Whether observer responded to stimulus */
  boolean gotAnswer = false;

  /**
   * Instantiate OpiLogic with a background, stimulus, and timer objects
   *
   * @since 0.0.1
   */
  OpiLogic(Background bgRecord, Stimulus stRecord, Timer timer) {
    this.bgRecord = bgRecord;
    this.stRecord = stRecord;
    this.timer = timer;
    // start background and fixation with defaults
    background = new Item(new Model(ModelType.SQUARE), new Texture(BLACK));
    background.size(180); // Background to cover the whole field of view
    fixation = new Item(new Model(ModelType.MALTESE), new Texture(BLACK));
    stimulus = new Item(new Model(ModelType.CIRCLE), new Texture(BLACK));
  }

  /* (non-Javadoc)
   * @see es.optocom.jovp.engine.PsychoLogic#init(es.optocom.jovp.engine.PsychoEngine)
   */
  @Override
  public void init(PsychoEngine psychoEngine) {
    // add perimetry items, background, fixation, stimulus
    items.add(background);
    items.add(fixation);
    stimulus.eye(Eye.NONE); // do not show until ready
    items.add(stimulus);
  }

  /* (non-Javadoc)
   * @see es.optocom.jovp.engine.PsychoLogic#input(es.optocom.jovp.engine.structures.Command, double)
   */
  @Override
  public void input(Command command) {
    // if no response or response is too early, do nothing
    if(command != Command.YES || ellapseTime < MINIMUM_TIME_FROM_ONSET) return;
    gotAnswer = true;
  }

  /* (non-Javadoc)
   * @see es.optocom.jovp.engine.PsychoLogic#update(es.optocom.jovp.engine.PsychoEngine)
   */
  @Override
  public void update(PsychoEngine psychoEngine) {
    // check if background and fixation settings have changed
    if(bgRecord != null) {
      setBackground();
      bgRecord = null; // set background record to null when done with it
      return;
    }
/**
    // if a stimulus not shown then return
    if(!onset) return; // TODO: bad management. Need to check if stimulus.eye == Eye.NONE
    // if a stimulus not shown then return: could be that presentation window
    // has expired or observer responded to it
    if(stSettings == null) return;
    
    { // if observer answered, then sit idly for a new stimulus
      stSettings = null; // set stimulus record to null when done with it`
      gotAnswer = false;
      return;
    }
*/
  }
 
  /**
   * Change background
   */
  private void setBackground() {
    // set new luminance and color in background
    bgRecord.bgLum();
    bgRecord.bgCol();
    background.setColor(bgRecord.bgCol());
    // TODO: fixation setType
    // set new position, size and rotation in fixation target
    fixation.position(bgRecord.fixCx(), bgRecord.fixCy());
    fixation.size(bgRecord.fixSx(), bgRecord.fixSy());
    fixation.rotation(bgRecord.fixRotation());
    // set new luminance and color in fixation target
    bgRecord.fixLum();
    bgRecord.fixCol();
    fixation.setColor(bgRecord.fixCol());
  }

  /**
   * Synchronized stimulus presentation
   */
  private void present() {
    // once done, set stimulus record to null
    stRecord = null;
  }

}
