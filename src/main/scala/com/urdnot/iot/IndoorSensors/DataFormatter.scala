package com.urdnot.iot.IndoorSensors

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DataFormatter extends DataStructures {
  def prepareInflux(structuredData: DataProcessor.IndoorSensor): Future[List[Option[String]]] = Future {
    List(
      structuredData.bmp280 match {
        case Some(x) => x.toInfluxString(structuredData.host, structuredData.timestamp)
        case None => None
      },
      structuredData.bme280 match {
        case Some(x) => x.toInfluxString(structuredData.host, structuredData.timestamp)
        case None => None
      },
      structuredData.bme680 match {
        case Some(x) => x.toInfluxString(structuredData.host, structuredData.timestamp)
        case None => None
      },
      structuredData.si1145 match {
        case Some(x) => x.toInfluxString(structuredData.host, structuredData.timestamp)
        case None => None
      },
      structuredData.sgp30 match {
        case Some(x) => x.toInfluxString(structuredData.host, structuredData.timestamp)
        case None => None
      },
      structuredData.tsl2561 match {
        case Some(x) => x.toInfluxString(structuredData.host, structuredData.timestamp)
        case None => None
      },
      structuredData.tsl2591 match {
        case Some(x) => x.toInfluxString(structuredData.host, structuredData.timestamp)
        case None => None
      }
    )
  }
}
