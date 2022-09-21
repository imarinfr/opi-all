package org.lei.opi.core.structures;

/**
 * Connection state
 *
 * @since 0.0.1
 */
public enum State {
  /** OPI connection ready */
  READY,
  /** OPI running */
  RUNNING,
  /** OPI connection is broken */
  BROKEN,
  /** OPI connection is closed */
  CLOSED
}
