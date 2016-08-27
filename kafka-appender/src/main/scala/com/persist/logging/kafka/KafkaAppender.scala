package com.persist.logging.kafka

import akka.actor.{ActorContext, ActorRefFactory, ActorSystem}
import com.persist.logging.{ClassLogging, LogAppender, LogAppenderBuilder, RichMsg}
import com.persist.JsonOps._
import java.util.Properties
import com.persist.logging._

import org.apache.kafka.clients.producer._

import scala.concurrent.Future

class CB extends Callback() with ClassLogging {
  var timeoutCnt = 0
  var sawTimeouts = false

  override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
    exception match {
      case ex: org.apache.kafka.common.errors.`TimeoutException` =>
        if (!sawTimeouts) log.error(JsonObject("fail" -> "KAFKA", "error" -> "Kafka timing out"))
        sawTimeouts = true
        timeoutCnt += 1
      case _ =>
        if (sawTimeouts) log.error(JsonObject("fail" -> "KAFKA", "error" -> "messages lost", "lost" -> timeoutCnt))
        sawTimeouts = false
        timeoutCnt = 0
    }
  }

  def finish(blockMs: Int) {
    Thread.sleep(blockMs * 3) // ait for all messages to complete
    if (timeoutCnt > 0) log.error(JsonObject("fail" -> "KAFKA", "error" -> "messages lost", "lost" -> timeoutCnt))
  }
}

object KafkaAppender extends LogAppenderBuilder {
  override def apply(factory: ActorRefFactory, standardHeaders: Map[String, RichMsg]): LogAppender =
    new KafkaAppender(factory, standardHeaders)
}

class KafkaAppender(factory: ActorRefFactory, standardHeaders: Map[String, RichMsg]) extends LogAppender {
  val cb = new CB()
  private[this] val system = factory match {
    case context: ActorContext => context.system
    case s: ActorSystem => s
  }
  private[this] implicit val executionContext = factory.dispatcher
  private[this] val config = system.settings.config.getConfig("com.persist.logging.appenders.kafka")
  private[this] val fullHeaders = config.getBoolean("fullHeaders")
  private[this] val sort = config.getBoolean("sorted")
  private[this] val topic = config.getString("topic")
  private[this] val bootstrapServers = config.getString("bootstrapServers")
  private[this] val acks = config.getString("acks")
  private[this] val retries = config.getInt("retries")
  private[this] val batchSize = config.getInt("batchSize")
  private[this] val blockMs = config.getInt("blockMs")
  private[this] val bufferMemory = config.getInt("bufferMemory")


  val props = new Properties()
  props.put("bootstrap.servers", bootstrapServers)
  props.put("acks", acks)
  props.put("retries", new Integer(retries))
  props.put("batch.size", new Integer(batchSize))
  //props.put("request.timeout.ms", new Integer(timeoutMs))
  props.put("max.block.ms", new Integer(blockMs))
  //props.put("linger.ms", new Integer(lingerMs))
  props.put("buffer.memory", new Integer(bufferMemory))
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  private[this] val producer: Producer[String, String] = new KafkaProducer(props)

  override def append(baseMsg: Map[String, RichMsg], category: String): Unit = {
    val msg = if (fullHeaders) standardHeaders ++ baseMsg else baseMsg
    val txt = Compact(msg, safe = true, sort = sort)
    Future {
      producer.send(new ProducerRecord[String, String](topic, txt), cb)
    }
  }

  override def finish(): Future[Unit] = {
    cb.finish(blockMs)
    Future.successful(())
  }

  override def stop(): Future[Unit] = {
    producer.close()
    Future.successful(())
  }
}
