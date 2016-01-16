package com.persist.logging

import akka.actor.{ActorSystem, ActorContext, ActorRefFactory}
import com.persist.JsonOps._
import scala.concurrent.Future

/**
 * Companion object for the StdOutAppender class.
 */
object StdOutAppender extends LogAppenderBuilder {
  /**
   * A constructor for the StdOutAppender class.
   * @param factory an Akka factory.
   * @param serviceName the name of the service.
   * @return the stdout appender.
   */
  def apply(factory: ActorRefFactory, serviceName: String) = new StdOutAppender(factory)
}

/**
 * A log appender that write common log messages to stdout.
 * @param factory an Akka factory.
 */
class StdOutAppender(factory: ActorRefFactory) extends LogAppender {
  private[this] val system = factory match {
    case context: ActorContext => context.system
    case s: ActorSystem => s
  }
  private[this] val config = system.settings.config.getConfig("com.persist.logging.appenders.stdout")
  private[this] val fullHeaders = config.getBoolean("fullHeaders")
  private[this] val color = config.getBoolean("color")
  private[this] val width = config.getInt("width")

  /**
   * Writes a log message to stdout.
   * @param stdHeaders the headers that are fixes for this service.
   * @param baseMsg the message to be logged.
   * @param category  the kinds of log (for example, "common").
   */
  def append(stdHeaders: Map[String, RichMsg], baseMsg: Map[String, RichMsg], category: String): Unit = {
    if (category == "common") {
      val msg = if (fullHeaders) stdHeaders ++ baseMsg else baseMsg
      val txt = Pretty(msg - "@category", safe = true, width=width)
      val colorTxt = if (color) {
        val level = jgetString(msg, "@severity")
        level match {
          case "FATAL" | "ERROR" =>
            s"${Console.RED}$txt${Console.RESET}"
          case "WARN" =>
            s"${Console.YELLOW}$txt${Console.RESET}"
          case _ => txt
        }
      } else {
        txt
      }
      println(colorTxt)
    }
  }

  /**
   * Closes the stdout appender.
   * @return a future that is completed when the close is complete.
   */
  def stop(): Future[Unit] = Future.successful(())
}
