package com.persist.logging.kafka

import akka.actor.{ActorSystem, ActorContext, ActorRefFactory}
import com.persist.logging.{RichMsg, LogAppenderBuilder, LogAppender}
import com.persist.JsonOps._
import java.util.Properties
import org.apache.kafka.clients.producer._

import scala.concurrent.Future

object KafkaAppender extends LogAppenderBuilder {
  override def apply(factory: ActorRefFactory, standardHeaders: Map[String, RichMsg]): LogAppender =
    new KafkaAppender(factory, standardHeaders)
}

class KafkaAppender(factory: ActorRefFactory, standardHeaders: Map[String, RichMsg]) extends LogAppender {
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
  private[this] val lingerMs = config.getInt("lingerMs")
  private[this] val bufferMemory = config.getInt("bufferMemory")


  val props = new Properties()
  props.put("bootstrap.servers", bootstrapServers)
  props.put("acks", acks)
  props.put("retries", new Integer(retries))
  props.put("batch.size", new Integer(batchSize))
  props.put("linger.ms", new Integer(lingerMs))
  props.put("buffer.memory", new Integer(bufferMemory))
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  private[this] val producer: Producer[String, String] = new KafkaProducer(props)

  override def append(baseMsg: Map[String, RichMsg], category: String): Unit = {
    val msg = if (fullHeaders) standardHeaders ++ baseMsg else baseMsg
    val txt = Compact(msg, safe = true, sort = sort)
    producer.send(new ProducerRecord[String, String](topic, txt))
  }

  override def stop(): Future[Unit] = {
    producer.close()
    Future.successful(())
  }
}
