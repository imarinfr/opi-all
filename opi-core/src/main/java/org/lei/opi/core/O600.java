package org.lei.opi.core;

import java.util.Map;

/**
 *
 * Octopus 600 client
 *
 * @since 0.0.1
 */
public class O600 extends OpiMachine {

  /* (non-Javadoc)
   * @see org.lei.opi.core.OpiMachine#query()
   */
  @Override
   public String query(Map<String, Object> args) {
    return super.query(args);
  }

  /* (non-Javadoc)
   * @see org.lei.opi.core.OpiMachine#init()
   */
  @Override
  public String initialize(Map<String, Object> args) {
    return super.initialize(args);
  }

  /* (non-Javadoc)
   * @see org.lei.opi.core.OpiMachine#setup()
   */
  @Override
  public String setup(Map<String, Object> args) {
    return super.setup(args);
  }

  /* (non-Javadoc)
   * @see org.lei.opi.core.OpiMachine#present()
   */
  @Override
  public String present(Map<String, Object> args) {
    return super.present(args);
  }

}