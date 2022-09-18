package org.lei.opi.jovp;

import java.io.IOException;

import org.junit.jupiter.api.Test;

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
    Settings settings;
    try {
      settings = Settings.defaultSettings(Settings.Machine.IMOVIFA);
      System.out.println(settings);
      settings = Settings.defaultSettings(Settings.Machine.PICOVR);
      System.out.println(settings);
      settings = Settings.defaultSettings(Settings.Machine.PHONEHMD);
      System.out.println(settings);
      settings = Settings.defaultSettings(Settings.Machine.DISPLAY);
      System.out.println(settings);
    } catch (IllegalArgumentException | IOException e) {
      throw new RuntimeException(e);
    }
  }

}
