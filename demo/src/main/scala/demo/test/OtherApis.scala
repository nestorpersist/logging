package demo.test

import java.net.InetAddress
import akka.actor.{Props, Actor, ActorSystem}
import com.persist.logging._
import logging_demo.BuildInfo
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await
import org.slf4j.LoggerFactory

case class Slf4jDemo() {
  val slf4jlog = LoggerFactory.getLogger(classOf[Slf4jDemo])

  def demo(): Unit = {
    slf4jlog.warn("slf4j")
  }
}

object AkkaActor {
  def props() = Props(new AkkaActor())
}

class AkkaActor() extends Actor with akka.actor.ActorLogging {
  def receive = {
    case "foo" => log.warning("Saw foo")
    case "done" => context.stop(self)
    case x: Any => log.error(s"Unexpected actor message: ${x}")
  }
}

case class AkkaDemo(system: ActorSystem) {
  def demo(): Unit = {
    val a = system.actorOf(AkkaActor.props(), name="Demo")
    a ! "foo"
    a ! "bar"
    a ! "done"
  }

}

object OtherApis {
  def main(args: Array[String]) {
    val system = ActorSystem("test")

    val host = InetAddress.getLocalHost.getHostName
    val loggingSystem = LoggingSystem(system, BuildInfo.name, BuildInfo.version, host)

    val slf = Slf4jDemo()
    slf.demo()

    val act = AkkaDemo(system)
    act.demo()

    Await.result(loggingSystem.stop, 30 seconds)
    Await.result(system.terminate(), 20 seconds)
  }
}

