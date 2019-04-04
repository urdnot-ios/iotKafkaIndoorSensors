package com.urdnot.iot

object dataObjects {
  final case class bmp280(
                         tempC: Option[Double],
                         tempF: Option[Double],
                         pressurePa: Option[Double],
                         pressureInHg: Option[Double],
                         altitudeM: Option[Double]
                         )
  final case class sgp30(
                        voc: Option[Int],
                        co2: Option[Int]
                        )
  final case class si1145(
                         vis: Option[Int],
                         ir: Option[Int],
                         uv: Option[Double]
                         )
  final case class airSensorsReading(
                                      timeStamp: Option[Long],
                                      bmp280Reading: Option[bmp280],
                                      sgp30Reading: Option[sgp30],
                                      si1145Reading: Option[si1145]
                                    )
  final case class bme280(
                           tempC: Option[Double],
                           tempF: Option[Double],
                           pressurePa: Option[Double],
                           pressureInHg: Option[Double]
                         )
  final case class ccs811 (
                          co2: Option[Int],
                          voc: Option[Int]
                          )
  final case class tsl2561(
                          lux: Option[Double]
                          )

  //{"timestamp":1552684129,"bmp280": {"tempC":26.22,"tempF":79.20,"PressurePa":101588.44,"PressureInHg":30.00,"altitudeM":116.26},"SGP30": {"TVOCPPB":20,"eCO2PPM":400},"SI1145": {"Vis":261,"IR":258,"UV":0.02}}

  //{"timestamp":1554182414, "host": "huzzah02", "bme280": {"tempC": 23.090000, "tempF": 73.561996, "PressurePa": 1006.303467,"PressureInHg": 29.716121},"ccs811": {"c02": 441, "voc": 6},"tsl2561": {"lux": 16.000000}}
}
