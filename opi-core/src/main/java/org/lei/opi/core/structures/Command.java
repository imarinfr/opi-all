package org.lei.opi.core.structures;

/**
 * OPI OpiMachine commands
 *
 * @since 0.0.1
 */
public enum Command {
  /** Choose OPI implementation */
  CHOOSE,
  /** Query device constants */
  QUERY,
  /** Setup OPI */
  SETUP,
  /** Initialize OPI connection */
  INITIALIZE,
  /** Present OPI static, kinetic, or temporal stimulus */
  PRESENT,
  /** Close OPI connection */
  CLOSE
}
