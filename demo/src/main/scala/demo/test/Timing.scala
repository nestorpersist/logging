package demo.test

import java.net.InetAddress
import akka.actor.ActorSystem
import com.persist.logging._
import logging_demo.BuildInfo
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits._

case class TimeClass() extends ClassLogging with Timing {
  def b(id: RequestId): Unit = {
    Time(id, "bbb") {
      Thread.sleep(100)
    }
  }

  def a(id: RequestId): Unit = {
    Time(id, "top") {
      Time(id, "aaa1") {
        Thread.sleep(200)
      }
      b(id)
      Time(id, "aaa2") {
        Thread.sleep(300)
      }
      b(id)
    }
  }
}

case class FutureClass() extends ClassLogging with Timing {
  def demo(id: RequestId): Future[Int] = {
    val token = time.start(id, "top")
    val f1 = Future {
      Time(id, "f1") {
        Thread.sleep(100)
        100
      }
    }
    val f2 = f1.map { i =>
      val result = Time(id, "f2") {
        Thread.sleep(200)
        Time(id, "f2a") {
           i * 2
        }
      }
      result
    }
    val f3 = f2.recover{ case ex:Throwable =>
      log.error("Timing test failed", ex)
      -1
    }
    f3.map {  i =>
      time.end(id, "top", token)
      i
    }
  }
}

object Timing {

  case class F() extends ClassLogging {
    val fc = FutureClass()
    val f = fc.demo(RequestId())
    val i = Await.result(f, 3 seconds)
    log.info(Map("@msg" -> "Future result", "val" -> i))
  }

  def main(args: Array[String]) {
    val system = ActorSystem("test")

    val host = InetAddress.getLocalHost.getHostName
    val loggingSystem = LoggingSystem(system, BuildInfo.name, BuildInfo.version, host)

    val tc = new TimeClass()
    tc.a(RequestId())
    tc.a(RequestId())

    F()


    Await.result(loggingSystem.stop, 30 seconds)
    Await.result(system.terminate(), 20 seconds)
  }
}