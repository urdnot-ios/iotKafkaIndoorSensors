package com.urdnot.iot

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import com.paulgoldbaum.influxdbclient.Parameter.Precision
import com.paulgoldbaum.influxdbclient.{Database, InfluxDB, Point}
import com.typesafe.config.Config
import com.typesafe.scalalogging.{LazyLogging, Logger}
import com.urdnot.iot.dataObjects.{bmp280, sgp30, si1145}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContextExecutor, Future}


object AirLightTempProcessor extends Directives with LazyLogging{

  implicit val system: ActorSystem = ActorSystem("iot_processor")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = materializer.executionContext
  val log: Logger = Logger("insideSensors")

  val consumerConfig: Config = system.settings.config.getConfig("akka.kafka.consumer")
  val envConfig: Config = system.settings.config.getConfig("env")
  val bootstrapServers: String = consumerConfig.getString("kafka-clients.bootstrap.servers")
  val consumerSettings: ConsumerSettings[String, Array[Byte]] =
    ConsumerSettings(consumerConfig, new StringDeserializer, new ByteArrayDeserializer)
      .withBootstrapServers(bootstrapServers)

  //Influxdb
  val influxdb: InfluxDB = InfluxDB.connect(envConfig.getString("influx.host"), envConfig.getInt("influx.port"))
  val database: Database = influxdb.selectDatabase(envConfig.getString("influx.database"))

  Consumer.committableSource(consumerSettings, Subscriptions.topics(envConfig.getString("kafka.topic")))
    .runForeach { x =>
      val record = x.record.value()
      val rawJson = record.map(_.toChar).mkString.replace("\'", "\"").replace("L", "")
      val parsedJson = Json.parse(rawJson)
      val host = (parsedJson \ "host").as[String]
      val timestamp = (parsedJson \ "timestamp").asOpt[Long]
      val bmp280: bmp280 = new bmp280(
        tempC = (parsedJson \ "bmp280" \ "tempC").asOpt[Double],
        tempF = (parsedJson \ "bmp280" \ "tempF").asOpt[Double],
        pressurePa = (parsedJson \ "bmp280" \ "PressurePa").asOpt[Double],
        pressureInHg = (parsedJson \ "bmp280" \ "PressureInHg").asOpt[Double],
        altitudeM = (parsedJson \ "bmp280" \ "altitudeM").asOpt[Double])
      val sgp30: sgp30 = new sgp30(
        voc = (parsedJson \ "SGP30" \ "TVOCPPB").asOpt[Int],
        co2 = (parsedJson \ "SGP30" \ "eCO2PPM").asOpt[Int])
      val si1145: si1145 = new si1145(
        vis = (parsedJson \ "SI1145" \ "Vis").asOpt[Int],
        ir = (parsedJson \ "SI1145" \ "IR").asOpt[Int],
        uv = (parsedJson \ "SI1145" \ "UV").asOpt[Double]
      )
      //{"timestamp":1552684129,"bmp280": {"tempC":26.22,"tempF":79.20,"PressurePa":101588.44,"PressureInHg":30.00,"altitudeM":116.26},"SGP30": {"TVOCPPB":20,"eCO2PPM":400},"SI1145": {"Vis":261,"IR":258,"UV":0.02}}


      val indoorAirTemp = Point("indoorBmp280", timestamp.getOrElse(0L))
        .addTag("sensor", "indoorBmp280")
        .addTag("host", host)
        .addField("tempC", bmp280.tempC.getOrElse(0.0))
        .addField("tempF", bmp280.tempF.getOrElse(0.0))
        .addField("PressurePa", bmp280.pressurePa.getOrElse(0.0))
        .addField("PressureInHg", bmp280.pressureInHg.getOrElse(0.0))
        .addField("altitudeM", bmp280.altitudeM.getOrElse(0.0))
      val indoorAirQuality = Point("indoorSGP30", timestamp.getOrElse(0L))
        .addTag("sensor", "indoorSGP30")
        .addTag("host", host)
        .addField("voc", sgp30.voc.getOrElse(0))
        .addField("co2", sgp30.co2.getOrElse(0))
      val indoorLight = Point("indoorSI2245", timestamp.getOrElse(0L))
        .addTag("sensor", "indoorSI2245")
        .addTag("host", host)
        .addField("visibleLight", si1145.vis.getOrElse(0))
        .addField("infraredLight", si1145.ir.getOrElse(0))
        .addField("ultravioletLight", si1145.uv.getOrElse(0.0))

      Future(database.write(indoorAirTemp, precision = Precision.SECONDS))
      Future(database.write(indoorAirQuality, precision = Precision.SECONDS))
      Future(database.write(indoorLight, precision = Precision.SECONDS))

    }
}
