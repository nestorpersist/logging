package demo.test

import java.net.InetAddress
import akka.actor.ActorSystem
import com.persist.logging._
import logging_demo.BuildInfo
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await

case class RequestB() extends ClassLogging {

  def demo(id: AnyId): Unit = {
    log.trace("In B", id = id)
    log info("BBB", id = id)
  }
}

case class RequestC() extends ClassLogging {
  def demo(id: AnyId): Unit = {
    log.trace("In C", id = id)
    log.info("CCC", id = id)
  }
}

case class RequestA() extends ClassLogging {
  val b = RequestB()
  val c = RequestC()

  def demo(id: AnyId): Unit = {
    log.trace("Enter A", id = id)
    b.demo(id)
    log.info("AAA", id = id)
    c.demo(id)
    log.trace("Exit A", id = id)
  }
}

object RequestIds {
  def main(args: Array[String]) {
    val system = ActorSystem("test")

    val host = InetAddress.getLocalHost.getHostName
    val loggingSystem = LoggingSystem(system, BuildInfo.name, BuildInfo.version, host)

    val a = new RequestA()
    a.demo(noId)
    a.demo(RequestId())
    a.demo(RequestId(level = Some(LoggingLevels.TRACE)))

    Await.result(loggingSystem.stop, 30 seconds)
    Await.result(system.terminate(), 20 seconds)
  }
}

