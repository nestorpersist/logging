package demo.test

import java.net.InetAddress
import akka.actor.ActorSystem
import com.persist.logging._
import logging_demo.BuildInfo
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

case class MyException(msg: RichMsg) extends RichException(msg)

case class ExceptionClass() extends ClassLogging {
  def demo(): Unit = {
    log.error("Test", new Exception("Bad Code"))
    log.warn("Rich", RichException(Map("@msg" -> "Fail", "count" -> 23)))
    log.error("Special", MyException(Map("@msg" -> "Fail", "count" -> 23)))
    try {
      throw MyException(Map("@msg" -> "Die", "name" -> "abc"))
    } catch {
      case ex: Exception => log.error("Caught exception", ex)
    }
  }
}

object Exceptions {
  def main(args: Array[String]) {
    val system = ActorSystem("test")

    val host = InetAddress.getLocalHost.getHostName
    val loggingSystem = LoggingSystem(system, BuildInfo.name, BuildInfo.version, host)

    val ec = new ExceptionClass()
    ec.demo()

    Await.result(loggingSystem.stop, 30 seconds)
    Await.result(system.terminate(), 20 seconds)
  }
}
