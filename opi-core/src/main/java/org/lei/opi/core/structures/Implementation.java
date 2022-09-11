package org.lei.opi.core.structures;

/**
 * OPI implementation: distributor for different perimeters or HMD devices
 *
 * @since 0.0.1
 */
public enum Implementation {
  /** Octopus 600 */
  O600,
  /** Octopus 900 */
  O900,
  /** Compass iCare perimeter */
  COMPASS,
  /** Kowa AP-7000 perimeter */
  KOWAAP7000,
  /** IMO perimeter through the jovp */
  IMO,
  /** PicoVR as perimeter through the jovp */
  PICOVR,
  /** Android phone as perimeter through the jovp */
  PHONEHMD
}
