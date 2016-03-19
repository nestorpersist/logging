# Scala Logging - Kafka Appender

This appender will send log messages to Kafka.
Each log message is written as compact Json.

### Documentation

 There are several kinds of documentation.

* This readme contains an overview of the Kafka Appendes.

* The src/test directory contains an example use 
of the kafka appender.

* The `kafka-appender/src/main/resources/kafkaAppender.conf` file documents all the configuration options and specifies
 their default values.

### Quick Start 

In addition to the logging jar you should also include

    "com.persist" % "kafka-appender_2.11" % "1.2.1"

Next add the following line to your `application.conf` file in
addition to logging.conf

    include "kafkaAppender.conf"

This can be followed by any overrides of Kafka appender defaults.






