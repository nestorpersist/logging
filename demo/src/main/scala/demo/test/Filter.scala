package demo.test

import java.net.InetAddress
import akka.actor.ActorSystem
import com.persist.logging._
import logging_demo.BuildInfo
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await

case class Class1() extends ClassLogging {
  def demo(name: String): Unit = {
    log.debug(name)
    log.warn(name)
  }
}

case class Class2() extends ClassLogging {
  def demo(name: String): Unit = {
    log.debug(name)
    log.warn(name)
  }
}

object Filter {

  import LoggingLevels._


  def filter(fields: Map[String, RichMsg]): Boolean = {
    val cls = fields.getOrElse("class", "") match {
      case s: String => s
      case _ => ""
    }
    val sevs = fields.getOrElse("@severity", "") match {
      case s: String => s
      case _ => ""
    }
    val sev = Level(sevs)
    if (cls == "demo.test.Class1") {
      sev >= DEBUG
    } else {
      sev >= WARN
    }
  }

  def main(args: Array[String]) {
    val system = ActorSystem("test")

    val host = InetAddress.getLocalHost.getHostName
    val loggingSystem = LoggingSystem(system, BuildInfo.name, BuildInfo.version, host)

    val c1 = new Class1()
    val c2 = new Class2()

    c1.demo("no filter")
    c2.demo("no filter")

    // Add filter and change level
    val oldLevel = loggingSystem.getLevel.current
    loggingSystem.setFilter(Some(filter))
    loggingSystem.setLevel(DEBUG)

    c1.demo("filter")
    c2.demo("filter")

    // Reset it back
    loggingSystem.setLevel(oldLevel)
    loggingSystem.setFilter(None)

    Await.result(loggingSystem.stop, 30 seconds)
    Await.result(system.terminate(), 20 seconds)
  }
}
