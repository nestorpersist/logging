package demo.test

import java.net.InetAddress
import akka.actor.ActorSystem
import com.persist.logging._
import logging_demo.BuildInfo
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await

case class AltClass() extends ClassLogging {

  def demo(): Unit = {
    log.alternative("foo", Map("message"->"test"))
    log.alternative("foo", Map("a" -> "x", "b" -> false, "c" -> 65))
    log.alternative("bar", Map("message"->"btest"))
  }
}

object Alternative {
  def main(args: Array[String]) {
    val system = ActorSystem("test")

    val host = InetAddress.getLocalHost.getHostName
    val loggingSystem = LoggingSystem(system, BuildInfo.name, BuildInfo.version, host)

    val alt = new AltClass()
    alt.demo()

    Await.result(loggingSystem.stop, 30 seconds)
    Await.result(system.terminate(), 20 seconds)
  }
}
