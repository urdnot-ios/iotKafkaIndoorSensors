package com.urdnot.iot.IndoorSensors

import com.typesafe.scalalogging.Logger
import com.urdnot.iot.IndoorSensors.DataFormatter.prepareInflux
import com.urdnot.iot.IndoorSensors.DataProcessor.parseRecord
import org.scalatest.flatspec.AsyncFlatSpec

class ParseJsonSuite extends AsyncFlatSpec with DataStructures {

  val validJsonHuzzah01_1: Array[Byte] = """{"timestamp":1599455145,"host": "huzzah01","bmp280": {"tempC":24.73,"tempF":76.51,"PressurePa":101012.81,"PressureInHg":29.83,"altitudeM":26.02},"SGP30": {"TVOCPPB":448,"eCO2PPM":726},"SI1145": {"Vis":261,"IR":254,"UV":0.02}}""".stripMargin.getBytes("utf-8")
  val validJsonHuzzah01_2: Array[Byte] = """{"timestamp":1599455147,"host": "huzzah01","bmp280": {"tempC":24.73,"tempF":76.51,"PressurePa":101013.70,"PressureInHg":29.83,"altitudeM":25.95},"SGP30": {"TVOCPPB":450,"eCO2PPM":752},"SI1145": {"Vis":262,"IR":253,"UV":0.02}}""".stripMargin.getBytes("utf-8")
  val inValidJsonHuzzah01_1: Array[Byte] = """{"timestamp:1599455147,"host": "huzzah01","bmp280": {"tempC":24.73,"tempF":76.51,"PressurePa":101013.70,"PressureInHg":29.83,"altitudeM":25.95},"SGP30": {"TVOCPPB":450,"eCO2PPM":752},"SI1145": {"Vis":262,"IR":253,"UV":0.02}}""".stripMargin.getBytes("utf-8")

  val validJsonHuzzah02_1: Array[Byte] = """{"timestamp":1599455139,"host": "huzzah02","bme280": {"tempC":25.74,"tempF":78.33,"PressurePa":101155.41,"PressureInHg":29.87,"altitudeM":14.13},"SGP30": {"TVOCPPB":963,"eCO2PPM":1134},"TSL2591": {"Vis":11,"IR":18}}""".stripMargin.getBytes("utf-8")
  //{"timestamp":1599455141,"host": "huzzah02","bme280": {"tempC":25.72,"tempF":78.30,"PressurePa":101152.08,"PressureInHg":29.87,"altitudeM":14.41},"SGP30": {"TVOCPPB":985,"eCO2PPM":1135},"TSL2591": {"Vis":11,"IR":18}}

  val validJsonHuzzah03_1: Array[Byte] = """{"timestamp":1599455162,"host": "huzzah03","bme280": {"tempC":25.97,"tempF":78.75,"PressurePa":101174.70,"PressureInHg":29.88,"altitudeM":12.52},"SGP30": {"TVOCPPB":589,"eCO2PPM":1016},"TSL2591": {"Vis":245,"IR":55}}""".stripMargin.getBytes("utf-8")
  //{"timestamp":1599455164,"host": "huzzah03","bme280": {"tempC":25.96,"tempF":78.73,"PressurePa":101177.34,"PressureInHg":29.88,"altitudeM":12.30},"SGP30": {"TVOCPPB":601,"eCO2PPM":1027},"TSL2591": {"Vis":245,"IR":55}}
  val log: Logger = Logger("rain")

  val validJsonReplyHuzzah01_1: IndoorSensor = IndoorSensor(timestamp = 1599455145, host = "huzzah01",
    bmp280 = Some(IndoorBmp280(tempC = Some(24.73), tempF = Some(76.51), pressurePa = Some(101012.81), pressureInHg = Some(29.83), altitudeM = Some(26.02))),
    bme280 = None,
    bme680 = None,
    si1145 = Some(indoorSI2245(Vis = Some(261), IR = Some(254), UV = Some(0.02))),
    ccs811 = None,
    sgp30 = Some(indoorSGP30(TVOCPPB = Some(448), eCO2PPM = Some(726))),
    tsl2561 = None,
    tsl2591 = None
  )
  val validJsonReplyHuzzah01_2: IndoorSensor = IndoorSensor(timestamp = 1599455147, host = "huzzah01",
    bmp280 = Some(IndoorBmp280(tempC = Some(24.73), tempF = Some(76.51), pressurePa = Some(101013.70), pressureInHg = Some(29.83), altitudeM = Some(25.95))),
    bme280 = None,
    bme680 = None,
    si1145 = Some(indoorSI2245(Vis = Some(262), IR = Some(253), UV = Some(0.02))),
    ccs811 = None,
    sgp30 = Some(indoorSGP30(TVOCPPB = Some(450), eCO2PPM = Some(752))),
    tsl2561 = None,
    tsl2591 = None
  )
  val validJsonReplyHuzzah02_1: IndoorSensor = IndoorSensor(timestamp = 1599455139, host = "huzzah02",
    bmp280 = None,
    bme280 = Some(IndoorBme280(tempC = Some(25.74), tempF = Some(78.33), pressurePa = Some(101155.41), pressureInHg = Some(29.87), altitudeM = Some(14.13))),
    bme680 = None,
    si1145 = None,
    ccs811 = None,
    sgp30 = Some(indoorSGP30(TVOCPPB = Some(963), eCO2PPM = Some(1134))),
    tsl2561 = None,
    tsl2591 = Some(indoorTsl2591(Vis = Some(11), IR = Some(18)))
  )
  val validJsonReplyHuzzah03_1: IndoorSensor = IndoorSensor(timestamp = 1599455162, host = "huzzah03",
    bmp280 = None,
    bme280 = Some(IndoorBme280(tempC = Some(25.97), tempF = Some(78.75), pressurePa = Some(101174.70), pressureInHg = Some(29.88), altitudeM = Some(12.52))),
    bme680 = None,
    si1145 = None,
    ccs811 = None,
    sgp30 = Some(indoorSGP30(TVOCPPB = Some(589), eCO2PPM = Some(1016))),
    tsl2561 = None,
    tsl2591 = Some(indoorTsl2591(Vis = Some(245), IR = Some(55)))
  )
  val validInfluxReplyHuzzah01_1 = List(
    None, None, None, None,
    Some("IndoorBmp280,host=huzzah01,sensor=IndoorBmp280 altitudeM=26.02,pressureInHg=29.83,pressurePa=101012.81,tempC=24.73,tempF=76.51 15994551450000"),
    Some("indoorSGP30,host=huzzah01,sensor=indoorSGP30 TVOCPPB=448,eCO2PPM=726 15994551450000"),
    Some("indoorSI2245,host=huzzah01,sensor=indoorSI2245 Vis=261,IR=254,UV=0.02 15994551450000")
  )

  val errorReply = Left("""expected : got 'host":...' (line 1, column 25)""")

  behavior of "DataFormatter"
  it should "prepare the influxdb update body " in {
    prepareInflux(validJsonReplyHuzzah01_1.asInstanceOf[DataProcessor.IndoorSensor]).map { x =>
      assert(x.sorted == validInfluxReplyHuzzah01_1)
    }
  }
  behavior of "parsedJson"
  it should "Parse out the JSON data structure from the JSON string" in {
    val futureJson1 = parseRecord(validJsonHuzzah01_1, log)
    val futureJson2 = parseRecord(validJsonHuzzah01_2, log)

    val futureJson3 = parseRecord(validJsonHuzzah02_1, log)

    val futureJson4 = parseRecord(validJsonHuzzah03_1, log)

    futureJson1 map { x =>
      assert(x == Right(validJsonReplyHuzzah01_1))
    }
    futureJson2 map { x =>
      assert(x == Right(validJsonReplyHuzzah01_2))
    }
    futureJson3 map { x =>
      assert(x == Right(validJsonReplyHuzzah02_1))
    }
    futureJson4 map { x =>
      assert(x == Right(validJsonReplyHuzzah03_1))
    }
  }
  it should "Return an error when there is a bad JSON string" in {
    val futureJson = parseRecord(inValidJsonHuzzah01_1, log)
    futureJson map { x =>
      assert(x == errorReply)
    }
  }
}
