package com.urdnot.iot.IndoorSensors

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{Authorization, BasicHttpCredentials}
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.Config
import com.typesafe.scalalogging.{LazyLogging, Logger}
import com.urdnot.iot.IndoorSensors.DataFormatter.prepareInflux
import com.urdnot.iot.IndoorSensors.DataProcessor.parseRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object IndoorSensors
  extends LazyLogging
    with DataStructures {

  implicit val system: ActorSystem = ActorSystem("indoor_sensors")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = materializer.executionContext
  val log: Logger = Logger("insideSensors")

  val consumerConfig: Config = system.settings.config.getConfig("akka.kafka.consumer")
  val envConfig: Config = system.settings.config.getConfig("env")
  val bootstrapServers: String = consumerConfig.getString("kafka-clients.bootstrap.servers")
  val consumerSettings: ConsumerSettings[String, Array[Byte]] =
    ConsumerSettings(consumerConfig, new StringDeserializer, new ByteArrayDeserializer)
      .withBootstrapServers(bootstrapServers)


  val INFLUX_URL: String = "http://" + envConfig.getString("influx.host") + ":" + envConfig.getInt("influx.port") + envConfig.getString("influx.route")
  val INFLUX_USERNAME: String = envConfig.getString("influx.username")
  val INFLUX_PASSWORD: String = envConfig.getString("influx.password")
  val INFLUX_DB: String = envConfig.getString("influx.database")
  val INFLUX_MEASUREMENT: String = ""
  val SOURCE_HOST: String = ""
  val SENSOR: String = ""


  Consumer
    .plainSource(consumerSettings, Subscriptions.topics(envConfig.getString("kafka.topic")))
    .map { consumerRecord =>
      parseRecord(consumerRecord.value(), log)
        .onComplete {
          case Success(x) => x match {
            case Right(valid) =>
              val dataFuture = prepareInflux(valid)
              dataFuture.onComplete {
                case Success(dataList) =>
                  dataList.foreach { dataOptional: Option[String] =>
                    dataOptional match {
                      case Some(data) =>
                        Http().singleRequest(HttpRequest(
                          method = HttpMethods.POST,
                          uri = Uri(INFLUX_URL).withQuery(
                            Query(
                              "bucket" -> INFLUX_DB,
                              "precision" -> "ns"
                            )
                          ),
                          headers = Seq(
                            Authorization(BasicHttpCredentials(INFLUX_USERNAME, INFLUX_PASSWORD))),
                          entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, data)
                        )).onComplete {
                          case Success(res) => res match {
                            case res if res.status.isFailure() => log.error(res.status.reason() + ": " + res.status.defaultMessage() + " Influx Message: " + res.headers(2))
                            case res if res.status.isSuccess() => log.debug(res.toString())
                            case _ => log.error("Influx failure: " + res)
                          }
                        }
                      case _ => None
                    }
                  }
                case Failure(e) => log.error("Unable to connect to Influx: " + e.getMessage)
              }
              valid
            case Left(invalid) => log.error(invalid)
          }
          case Failure(exception) => log.error(exception.getMessage)
        }
    }
    .toMat(Sink.ignore)(DrainingControl.apply)
    .run()
}
