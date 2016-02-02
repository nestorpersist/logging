package com.persist.logging.test

import java.net.InetAddress
import akka.actor.ActorSystem
import com.persist.logging.kafka.KafkaAppender
import com.persist.logging._
import logging_demo.BuildInfo
import scala.language.postfixOps
import scala.concurrent.duration._
import scala.concurrent.Await

case class TestKafka() extends ClassLogging {
  def send: Unit = {
    for (i <- 1 to 5) {
      log.warn(Map("@msg" -> "test", "i" -> i))
      Thread.sleep(2000)
    }
  }
}

object TestKafka {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("test")

    val host = InetAddress.getLocalHost.getHostName
    val loggingSystem = LoggingSystem(system, BuildInfo.name,
      BuildInfo.version, host, appenderBuilders = Seq(StdOutAppender, KafkaAppender))

    val tc = TestKafka()
    tc.send

    Await.result(loggingSystem.stop, 30 seconds)
    Await.result(system.terminate(), 20 seconds)
  }
}
