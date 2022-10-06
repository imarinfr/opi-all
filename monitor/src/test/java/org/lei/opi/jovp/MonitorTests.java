package org.lei.opi.jovp;

import org.junit.jupiter.api.Test;
import org.lei.opi.monitor.OpiMonitor;

/**
 *
 * Unitary tests for monitor
 *
 * @since 0.0.1
 */
public class MonitorTests {
  
    /**
   * Monitor controlling Display on stereoscopic view
   *
   * @since 0.0.1
   */
  @Test
  public void launchMonitor() {
    String[] args = new String[] {"false", "true"};
    OpiMonitor opiM = new OpiMonitor(50001);
    opiM.launch();
  }
}
