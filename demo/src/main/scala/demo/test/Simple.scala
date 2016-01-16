package demo.test

import java.net.InetAddress
import akka.actor.ActorSystem
import com.persist.logging._
import logging_demo.BuildInfo
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await

case class SimpleClass() extends ClassLogging {

  def demo(): Unit = {
    log.info("Test")
    log.error("Foo failed")
    log.warn(Map("@msg" -> "fail", "value" -> 23))
  }

}

object Simple {
  def main(args: Array[String]) {
    val system = ActorSystem("test")

    val host = InetAddress.getLocalHost.getHostName
    val loggingSystem = LoggingSystem(system, BuildInfo.name, BuildInfo.version, host)

    val sc = new SimpleClass()
    sc.demo()

    Await.result(loggingSystem.stop, 30 seconds)
    Await.result(system.terminate(), 20 seconds)
  }
}
