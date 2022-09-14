package org.lei.opi.jovp;
import org.junit.jupiter.api.Test;
import org.lei.opi.jovp.Configuration.Impl;

/**
 *
 * Unitary tests for default configuration files
 *
 * @since 0.0.1
 */
public class DefaultConfigurationTests {

  /**
   *
   * Load default configuration files
   *
   * @since 0.0.1
   */
  @Test
  public void defaultJsonConfs() {
    Configuration conf = new Configuration(Impl.IMO);
    conf = new Configuration(Impl.PC);
    conf = new Configuration(Impl.PHONEHMD);
    conf = new Configuration(Impl.PICOVR);
  }

  /**
   *
   * Load incorrect configuration files
   *
   * @since 0.0.1
   */
  @Test
  public void wrongJsonConf() {

  }

}
