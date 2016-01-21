package demo.test

import java.net.InetAddress
import akka.actor.{ActorRefFactory, ActorSystem}
import com.persist.logging._
import logging_demo.BuildInfo
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Future

case class AppenderClass() extends ClassLogging {

  def demo(): Unit = {
    log.info("Test")
    log.error("Foo failed")
    log.warn(Map("@msg" -> "fail", "value" -> 23))
  }
}

object FlatAppender extends LogAppenderBuilder {
  def apply(factory: ActorRefFactory, stdHeaders: Map[String, RichMsg])
  = new FlatAppender(factory, stdHeaders)
}

class FlatAppender(factory: ActorRefFactory, stdHeaders: Map[String, RichMsg]) extends LogAppender {

  def append(msg: Map[String, RichMsg], category: String) {
    if (category == "common") {
      val level = msg.get("@severity") match {
        case Some(s: String) => s
        case _ => "???"
      }
      val time = msg.get("@timestamp") match {
        case Some(s: String) => s
        case _ => "???"
      }
      val message = richToString(msg.getOrElse("msg","???"))
      println(s"$time\t$level\t$message")
    }
  }

  def stop(): Future[Unit] = Future.successful(())
}

object Appender {
  def main(args: Array[String]) {
    val system = ActorSystem("test")

    val host = InetAddress.getLocalHost.getHostName
    val loggingSystem = LoggingSystem(system, BuildInfo.name,
      BuildInfo.version, host, appenderBuilders = Seq(FileAppender, FlatAppender))

    val sc = new SimpleClass()
    sc.demo()

    Await.result(loggingSystem.stop, 30 seconds)
    Await.result(system.terminate(), 20 seconds)
  }
}
