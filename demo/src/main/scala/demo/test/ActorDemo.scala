package demo.test

import java.net.InetAddress
import akka.actor.{Props, Actor, ActorSystem}
import com.persist.logging._
import logging_demo.BuildInfo
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await


object DemoActor {
  def props() = Props(new DemoActor())
}

class DemoActor() extends Actor with ActorLogging {
  println(this.getClass.getSimpleName)

  def receive = {
    case "foo" => log.info("Saw foo")
    case "done" => context.stop(self)
    case x: Any => log.error(Map("@msg" -> "Unexpected actor message",
      "message" -> x.toString))
  }
}

case class ActorDemo(system: ActorSystem) {
  def demo(): Unit = {
    val a = system.actorOf(DemoActor.props(), name = "Demo")
    a ! "foo"
    a ! "bar"
    a ! "done"
  }
}

object ActorDemo {
  def main(args: Array[String]) {
    val system = ActorSystem("test")

    val host = InetAddress.getLocalHost.getHostName
    val loggingSystem = LoggingSystem(system, BuildInfo.name, BuildInfo.version, host)

    val act = ActorDemo(system)
    act.demo()

    Await.result(loggingSystem.stop, 30 seconds)
    Await.result(system.terminate(), 20 seconds)
  }
}

