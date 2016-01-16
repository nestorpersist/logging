package com.persist.logging

import com.persist.JsonOps._

/**
 * This companion object for the RichException class. You might want to turn off
 * Throwable methods.
 */
object RichException {

  /**
   * Apply method for RichException.
   * @param richMsg the rich exception message.
   * @return the RichException
   */
  def apply(richMsg:RichMsg) = new RichException(richMsg)

  /** The unapply for matching the RichException trait.
    */
  def unapply(f: RichException): Option[(Any)] = Some((f.richMsg))

  private def stringify(j: Any): String = {
    j match {
      case s: String => s
      case j: Any => Compact(j)
    }
  }
}

/**
 * The common parent of all rich exceptions.
 *
 * @param richMsg the rich exception message.
 */
class RichException(val richMsg: RichMsg) extends Exception(RichException.stringify(richMsg))

