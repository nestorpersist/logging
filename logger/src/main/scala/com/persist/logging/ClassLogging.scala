package com.persist.logging

/**
 * This trait should be included in Scala (non-actor) classes to enable logging.
 * Click the visibility All button to see protected
 * members that are defined here.
 */

trait ClassLogging {

  /**
   * The framework logger.
   */
  protected lazy val log = new Logger()

}
