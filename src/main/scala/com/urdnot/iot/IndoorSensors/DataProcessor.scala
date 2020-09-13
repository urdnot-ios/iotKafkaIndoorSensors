package com.urdnot.iot.IndoorSensors


import com.typesafe.scalalogging.Logger
import io.circe.parser._
import io.circe.{Json, ParsingFailure}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DataProcessor extends DataStructures {
  def parseRecord(record: Array[Byte], log: Logger): Future[Either[String, IndoorSensor]] = Future {
    val recordString = record.map(_.toChar).mkString
    val genericParse: Either[ParsingFailure, Json] = parse(recordString)
    import io.circe.optics.JsonPath._
    genericParse match {
      case Right(x) => x match {
        case x: Json =>
          /*
          BEWARE SYNTAX! This does not check for typos, if caps are wrong or anything is off, it won't tell you.

          Worked over this several times before arriving at this design. The inbound data comes from sensor arrays that have different sensors on them
          I figured my options were:

          1-drop the sensor name and normalize the data
          2-build pre-configured profiles for each sensor array and parse based on the profile
          3-build a generic parser that checks for each sensor type

          I felt option 1 was too early in the overall flow, that kind of normalization should be closer to the data algorithms.
          This means no matter what I have to build something for each sensor in the system and add/modify as things change.

          I decided on 3 so that I could consolidate the sensor information in the data structures object.

          The goal is to check for each of the sensor case classes root inside the JSON string, parse them if they exist, return None if they don't.
          The steps are:
          1-if you don't get timestamp or host you are broke, go home
          2-For the rest, check if that parent data label is there, if so, unwrap
          3-Return a fully structured/optional'ed structure
           */
          try {
            Right(IndoorSensor(
              timestamp = root.timestamp.long.getOption(x).get,
              host = root.host.string.getOption(x).get,
              bmp280 = root.bmp280.json.getOption(x) match {
                case Some(_) => Some(
                  IndoorBmp280(
                    tempC = root.bmp280.tempC.double.getOption(x),
                    tempF = root.bmp280.tempF.double.getOption(x),
                    pressurePa = root.bmp280.PressurePa.double.getOption(x),
                    pressureInHg = root.bmp280.PressureInHg.double.getOption(x),
                    altitudeM = root.bmp280.altitudeM.double.getOption(x)))
                case _ => None
              },
              bme280 = root.bme280.json.getOption(x) match {
                case Some(_) =>
                  Some(IndoorBme280(
                    tempC = root.bme280.tempC.double.getOption(x),
                    tempF = root.bme280.tempF.double.getOption(x),
                    pressurePa = root.bme280.PressurePa.double.getOption(x),
                    pressureInHg = root.bme280.PressureInHg.double.getOption(x),
                    altitudeM = root.bme280.altitudeM.double.getOption(x)))
                case _ => None
              },
              bme680 = root.bme680.json.getOption(x)  match {
                case Some(_) => Some(IndoorBme680(
                  tempC = root.bme680.tempC.double.getOption(x),
                  pressurePa = root.bme680.pressurePa.double.getOption(x),
                  altitudeMeters = root.bme680.altitudeMeters.double.getOption(x),
                  gasOhms = root.bme680.gasOhms.int.getOption(x),
                  pressureInHg = root.bme680.pressureInHg.double.getOption(x),
                  tempF = root.bme680.tempF.double.getOption(x),
                  humidity = root.bme680.humidity.double.getOption(x)))
                case _ => None
              },
              si1145 = root.SI1145.json.getOption(x) match {
                case Some(_) => Some(Si1145(
                  Vis = root.SI1145.Vis.int.getOption(x),
                  IR = root.SI1145.IR.int.getOption(x),
                  UV = root.SI1145.UV.double.getOption(x)))
                case _ => None
              },
              ccs811 = root.ccs811.json.getOption(x) match {
                case Some(_) => Some(Ccs811(
                  co2 = root.ccs811.co2.int.getOption(x),
                  voc = root.ccs811.voc.int.getOption(x)))
                case _ => None
              },
              sgp30 = root.SGP30.json.getOption(x) match {
                case Some(_) => Some(Sgp30(
                  TVOCPPB = root.SGP30.TVOCPPB.int.getOption(x),
                  eCO2PPM = root.SGP30.eCO2PPM.int.getOption(x)))
                case _ => None
              },
              tsl2561 = root.TSL2561.json.getOption(x) match {
                case Some(_) => Some(Tsl2561(
                  lux = root.TSL2561.lux.double.getOption(x)))
                case _ => None
              },
              tsl2591 = root.TSL2591.json.getOption(x) match {
                case Some(_) => Some(Tsl2591(
                  Vis = root.TSL2591.Vis.int.getOption(x),
                  IR = root.TSL2591.IR.int.getOption(x)))
              case _ => None
              }
            ))
          } catch {
            case e => Left("Unable to parse: " + e.getMessage)
          }
        case _ => Left("I dunno what this is, but it's not a huzzah01 message: " + x)
      }
      case Left(x) => Left(x.getMessage)
    }
  }
}
