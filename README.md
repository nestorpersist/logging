# Scala Logging
[![Latest version](https://index.scala-lang.org/nestorpersist/logging/persist-logging/latest.svg)](https://index.scala-lang.org/nestorpersist/logging/persist-logging)
[![Maven Central](https://img.shields.io/maven-central/v/com.persist/persist-logging_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.persist/persist-logging_2.12)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This library provides an advanced logging facility for Scala applications.
It has the following features.

* All log messages are routed to a single Akka Actor.
* It captures logging messages from its own advanced Scala API, Java Slf4j and Akka loggers and sends them to that actor.
* All messages are logged by default as Json, but other formats can be defined.
* In the Scala API messages can be maps rather then just strings.
* The Scala API, supports logging exceptions and their stack trace as Json.
* Logs can be sent to stdout, local log files or a user defined appender.
* Stdout messages are logged with abbreviated info in a pretty format.
Other logs log one message per line.
* Log messages have a timestamp set at the time of the log call.
* Logging within a Scala class logs the class name and line number.
* Logging inside an Akka actor also logs the actor path.
* The logging level can be set dynamically.
* The logging level can be overriden on a per id basis.
* There is an optional logger that will log garbage collection events.
* There is an optional time logger that can be used to track subtimes
of a complex operation. This can be used across
futures and actor message sends.
* There is an "alternative" logger that can be used to create custom logs.
* A user defined filter can be used to control which messages are logged. This filter can be modified at run-time.
* The Scala API supports request ids that can be used to track a single request across multiple services.
* New log appenders can be defined and can be used in place of the built-in stdout and file
appender. These appenders can write in any format (including non Json ones) and can
send logs to other services (such as Kafka or Flume).

### Documentation

 There are several kinds of documentation.

* This readme contains an overview of all features.

* [ScalaDoc](http://nestorpersist.github.io/logging/latest/api/com/persist/logging/index.html)

* The [demo](https://github.com/nestorpersist/logging/tree/master/demo/src/main/scala/demo/test)
subdirectory contains a project with sample uses of the logging api.
* The [logging.conf](https://github.com/nestorpersist/logging/blob/master/logger/src/main/resources/logging.conf)
file documents all the configuration options and specifies their default values.

### Quick Start (see demo [Simple.scala](https://github.com/nestorpersist/logging/blob/master/demo/src/main/scala/demo/test/Simple.scala))

The Scala logger contains lots of advanced features,  but this
section introduces the simple core features that will
get you up and going fast.

First you must include the logging jar

    "com.persist" % "persist-logging_2.12" % "1.3.1"
    
    "com.persist" % "persist-logging_2.11" % "1.2.5"
    
Next add the following line to your `application.conf` file

    include "logging.conf"

In your Scala files that use logging add the following line

    import com.persist.logging._

At the beginning of your program, initialize the logging
system as follows

    val system = ActorSystem("test")
    val loggingSystem = LoggingSystem(system,"myproject", "1.0.2","myhost.com")

See the Simple demo code for a nice way
to initialize these parameters using
the sbt buildinfo plugin.

At the end of your program, shut down the logging and actor systems

    Await.result(loggingSystem.stop, 30 seconds)
    Await.result(system.terminate(), 30 seconds)

Inherit trait `ClassLogging` in classes to define logging methods.
Inside those classes you can then call

    log.error("Foo failed")

or you can use a map for the msg

    log.warn(Map("@msg"->"fail","value"->23))

By default, log messages will be written in a pretty Json form
to stdout and in a compact (1 message per line) Json form in log files.

### Configuration

The default configuration is in the file

    src/main/resources/logging.conf

You should include this in your `application.conf` file.

    include "logging.conf"

This can then be followed by any of your overrides of the
default configuration setting.

The demo directory contains a sample `application.conf` file.

### Basic Logging

Logging is enabled by inheriting the trait `ClassLogging`
(see demo 
[Simple.scala](https://github.com/nestorpersist/logging/blob/master/demo/src/main/scala/demo/test/Simple.scala)).
in classes and the
trait `ActorLogging` in actors (see demo 
[ActorDemo.scala](https://github.com/nestorpersist/logging/blob/master/demo/src/main/scala/demo/test/ActorDemo.scala)).

A typical log call takes the form

    log.warn(msg,ex=ex,id=id)

* **msg.** The msg can be either a string or a map.
Use the map form rather than string interpolation. When using
the map form by convention use the field name `@msg` for
the main error message. This ensures in sorted output it will
appear first.

```
    log.warn(s"Size $size is too big")
    log.warn(Map("@msg"->"Size is too big","size"->size))
```    

* **ex.** Optional exception. 
A `RichException` allows
its message to be either a string or a map.
The logger can expand that map into the logged Json.
Ordinary Scala/Java exceptions
will, of course, also work just fine. 

```
    try {
         ...
         throw RichException(Map("@msg"->"Size is too big", "size"->size))
         ...
    } catch {
         case ex => log.error("Body failed", ex)
    }
```    

* **id.** Optional id. See the section below on Request Ids.

The full set of logging levels are trace, debug, info, warn, error
and fatal.

### Alternative Logging (see demo [Alternative.scala](https://github.com/nestorpersist/logging/blob/master/demo/src/main/scala/demo/test/Alternative.scala))

You can create your own log files.

    log.alternative("extra", Map("a"->"foo", "b"-> 5.6))

This will log a message to the extra log. If no extra log previously
exists it will be created.
Alternative messages are by default not logged to stdout.

### Rich Messages

The rich message in logging calls and in RichExceptions
can be either a string or a map from strings to values.

But rich messages are even more general.
A rich message can be

* String
* Boolean
* Long
* Double
* BigDecimal
* null
* Seq[RichMsg]
* Map[String,RichMsg]

### Standard Fields

Logs can contain the following fields.
These are listed in alphabetical order
(this is the order displayed by the Json pretty printer).

* **@category.** The kind of log file: common, gc, time, or alterative
  log name.
* **@host.** The local host name.
* **@service.** The name and version of the application being run.
* **@severity.** The message level: trace, debug, info, warn, error or fatal.
* **@timestamp.** The time the message was logged.
* **@traceId.** The request id, if specified. 
* **@version.** The logging system version. Currently 1.
* **actor.** The path for an actor when using ActorLogging.
* **class.** The class for ClassLogging or ActorLogging.
* **file.** The file containing the log call.
* **kind.** Either slf4j or akka.  Not present for logger API.
* **line.** The line where the message was logged.
* **msg.** The rich message.
* **trace.** Information for any exception specfied in the logging call.

The fields @host, @service, and @version are the same for all messages
from a single application and are only present in logs when the
fullHeaders option is specified. This option is off by default for
stdout and on by default for files. The @category option is also
not present in stdout (because it contains only common messages).

### Appenders (see demo [Appender.scala](https://github.com/nestorpersist/logging/blob/master/demo/src/main/scala/demo/test/Appender.scala))

Appenders output log messages.
There are tow build-in appenders: stdout and file
plus an optional Kafka appenders.
Each of these has its own configuration options
(see `logging.conf` for details).
You can also define your own appenders.

The set of appenders to use is specified via the optional
`appenderBuilders` parameter to the `LoggingSystem` constructor.

*  **stdout.** Common log messages are written in Pretty Json form without
standard headers.
This appender is useful during development. It is usually
   disabled in production
* **file.** Log messages are written as Json, one per line.
   Ordinary log messages are written to the common log files.
Alterative, garbage collection, and timing messages have their own log files.
Each log file includes a day as part of its name. Each day a new file
is created.
* **Kafka.** The kafka-appender directory contains a Kafka appender.
See the README there for details on how to use.
* **custom.** Custom appenders implement the `LogAppender` and
`LogAppenderBuilder` traits. Custom loggers could be build for
Flume. Also note that custom loggers need not output
Json but can instead have their own custom format.

### Json Utilities

Log files with one Json message per line can be hard to read.
To make this easier, you can use the Json Pretty utility which you
can get at <<https://github.com/nestorpersist/json-tools/commits?author=nestorpersist>>

To view a log.
 
    cat common.2016-01-10.log | Pretty | less 

To convert a log file to a Pretty version of that file.

    Pretty < common.2016-01-10.log > common.2016-01-10.pretty

### Akka Logging (see demo [OtherApis.scala](https://github.com/nestorpersist/logging/blob/master/demo/src/main/scala/demo/test/OtherApis.scala))

Akka includes its own logger which can be added to Actors with the
trait `akka.actor.ActorLogging`.
These messages will be send to the common log with kind set to "akka".

Akka logging has a minumum logging level with default `warning`.
No messages with a level less than that mimimum level will ever be logged.
You can change that minimum level by
changing the value of the configuration value of
`akka.loglevel`.

See the section below on logging levels 
on how to more dynamically set the akka 
logging level higher than the minimum. 

### Java Logging (see demo [OtherApis](https://github.com/nestorpersist/logging/blob/master/demo/src/main/scala/demo/test/OtherApis.scala))

If you include Java, code in your application, you will often find it
uses Slf4J to do its logging.
Slf4J is routed to logback that in turn sends Slf4j messages to the
common log with kind set to "slf4j".

Slf4J logging has a minimum logging level with the default `WARN`.
No messages with a level less than that minimum level will ever be
logged.
You can change that minimum level by setting the following Java
property when launching the JVM.

    -DSLF4J_LEVEL=INFO

If you are using other loggers such as log4j, you can route those 
to slf4j using a bridge.  See <http://www.slf4j.org/legacy.html>.

 See the section below on logging levels 
on how to more dynamically set the Slf4J 
logging level higher than the minimum. 

### Log Levels

There are separate log levels for the logging API,
Akka logging, and Slf4J. The initial
value of these is set in the configuration file.
Default values appear in the `logging.conf` file
and can be overriden in the `application.conf` file.

These values can be changed dynamically by
calling methods on the `LoggingSystem` class.

The log level also be changed on individual requests
(see the section below on request ids).

### Log Filters (see demo [Filter.scala](https://github.com/nestorpersist/logging/blob/master/demo/src/main/scala/demo/test/Filter.scala))

Logging filters provide a very powerful way of filtering common log
messages. When a filter is enabled, a user specified function
can see all the fields of the message and decide if
that message is to be logged or ignored.

A logging filter is enabled and disabled by
call the `setFilter` method of `LoggingSytem`.

A logging filter by itself can only reduce the number of messages logged,
but it can also be used as part of a pattern to increase the number of
messages logged. Suppose that the current logging level is `info` but a you want to
also log all `debug` messages for class `Foo`.

1. Add a logging filter (by calling `logSystem.setFilter`) that permits all messages that are either
  * in class `Foo` with  level at least `debug` or
  * have a logging level of at least `info`.
2. Lower the logging level from `info` to `debug` by calling
` logSystem.setLevel`.

Note that logging filters are powerful, but do
introduce additional processing overhead.

### Garbage Collection Logging

Garbage collection events are logged to the gc log when
enabled by the `gc` configuration option.

### Request Ids (see demo [RequestIds.scala](https://github.com/nestorpersist/logging/blob/master/demo/src/main/scala/demo/test/RequestIds.scala))

Request Ids are based on the ideas from
Google Dapper

<http://research.google.com/pubs/pub36356.html>

and Twitter Zipkin

<https://twitter.github.io/zipkin/>.

The basic idea is that individual requests into a distributed
service-orriented architecture can be tracked within and
across services. Information contained in the log files of the
set of services is combined to yeild a complete picture
of each individual request.

A request Id contains three parts:

* **trackingId.** This value should be the same for all log calls
across all services for a specific request. A guid is often used for
this purpose.
* **spanId.** Suppose that for a given request to service A, it calls
service B multiple times. The spanId should be different on each call
to B.
* **level**. An optional field that can be used to change the log
level of an individual request. A message is logged if its level is
greater or equal to either the common log level or the level specified
in the request id. This permits more detailed logging of single
request without having to increase the log level for all requests.

To implement full distributed tracing, in addition to the
this logger, two other things are needed.

* Two log files (these can be created with `log.alternative`)
   * A server log that logs the request ids of all incoming requests
   to a service.
   * a client log that logs the request ids of outgoing requests from
   a service to other services.

* An analysis program that can join information across logs and
   display them in a usable form.

### Timing Logging (see demo [Timing.scala](https://github.com/nestorpersist/logging/blob/master/demo/src/main/scala/demo/test/Timing.scala))

Time logging provides a simple way to get fine grain timing of
dynamically nested blocks of code.
Timing is only enabled when the configuation value
`com.persist.logging.time` is set to true.
Results are written to `time` log where times are
reported in microseconds.

To add timing calls to you code, you must include trait
`Timing`.
Surround each block of code with a call to `Time`.

For cases, such as futures, where nesting is not possible
instead call `time.start` and `time.end`.

### Persist Json

Persist Json is a light-weight high-performance Scala Json library.
It is used in the implementation of the logger.

The public logger APIs do not use Persist Json. So you are not required
to use Persist Json in your code to use the logger.

Note however that Persist Json parse trees are rich messages.
In particular, Persist Json parse trees consist only of
build-in Scala types and do not define any new classes.
Persist Json provides a extensive set of features beyond what
the logger provides for working
with rich messages.

### Acknowledgements 

An earlier version of this logger was produced by [Whitepages] 
(http://www.whitepages.com). 
This earlier code can be found at 
[Framework](https://github.com/whitepages/WP_Scala_Framework/tree/master/logging). 

Work improving this logger and making it suitable for general use was 
supported by [47 Degrees](http://www.47deg.com). 






