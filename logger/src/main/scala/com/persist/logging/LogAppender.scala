package com.persist.logging

import akka.actor.ActorRefFactory
import scala.concurrent.Future

/**
 * Trait for log appender companion objects.
 */
trait LogAppenderBuilder {
  /**
   * Log appender constructor.
   * @param factory an Akka factory.
   * @param serviceName the name of the service.
   * @return
   */
  def apply(factory: ActorRefFactory, serviceName: String): LogAppender
}

/**
 * Trait for log appender classes.
 */
trait LogAppender {

  /**
   * Appends a new log message.
   * @param stdHeaders the headers that are fixes for this service.
   * @param baseMsg the message to be logged.
   * @param category  the kinds of log (for example, "common").
   */
  def append(stdHeaders: Map[String, RichMsg], baseMsg: Map[String, RichMsg], category: String)

  /**
   * Stops a log appender.
   * @return a future that is completed when stopped.
   */
  def stop(): Future[Unit]

}


