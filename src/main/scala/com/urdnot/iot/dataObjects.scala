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
  //
  //{"timestamp":1552684129,"bmp280": {"tempC":26.22,"tempF":79.20,"PressurePa":101588.44,"PressureInHg":30.00,"altitudeM":116.26},"SGP30": {"TVOCPPB":20,"eCO2PPM":400},"SI1145": {"Vis":261,"IR":258,"UV":0.02}}
}
