package org.lei.opi.jovp;

import es.optocom.jovp.PsychoEngine;
import es.optocom.jovp.PsychoLogic;
import es.optocom.jovp.structures.Command;

  /**
   * The OPI JOVP logic
   *
   * @since 0.0.1
   */
public class Logic implements PsychoLogic {

  /* (non-Javadoc)
   * @see es.optocom.jovp.engine.PsychoLogic#init(es.optocom.jovp.engine.PsychoEngine)
   */
  @Override
  public void init(PsychoEngine psychoEngine) {
    // TODO Required initialization
  }

  /* (non-Javadoc)
   * @see es.optocom.jovp.engine.PsychoLogic#input(es.optocom.jovp.engine.structures.Command, double)
   */
  @Override
  public void input(Command command, double time) {
    // TODO check for instance fast clicks.
  }

  /* (non-Javadoc)
   * @see es.optocom.jovp.engine.PsychoLogic#update(es.optocom.jovp.engine.PsychoEngine)
   */
  @Override
  public void update(PsychoEngine psychoEngine) {
    // TODO Update stimuli as necessary
  }
  
}
