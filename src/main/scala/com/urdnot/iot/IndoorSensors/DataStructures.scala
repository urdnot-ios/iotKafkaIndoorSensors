package com.urdnot.iot.IndoorSensors

trait DataStructures {

  /**
   * Master class of the different sensors. Host and timestamp are mandatory.
   *
   * @param host
   * @param timestamp
   * @param bmp280
   * @param bme280
   * @param bme680
   * @param si1145
   * @param ccs811
   * @param sgp30
   * @param tsl2561
   * @param tsl2591
   */
  final case class IndoorSensor(host: String,
                                timestamp: Long,
                                bmp280: Option[IndoorBmp280],
                                bme280: Option[IndoorBme280],
                                bme680: Option[IndoorBme680],
                                si1145: Option[Si1145],
                                ccs811: Option[Ccs811],
                                sgp30: Option[Sgp30],
                                tsl2561: Option[Tsl2561],
                                tsl2591: Option[Tsl2591]
                               )

  /**
   * The "toInfluxString" method assumes you have validated th inbound optionals. It is a convenience method to prepare the data for
   * Insertion into InfluxDB, following the pattern of "toString." Any changes to the Influx structure must be made here.
   * The Influx pattern is: Measurement,field=value,field=value tag=value,tag=value timestamp
   * Spaces and commas and = signs all matter
   * @param tempC
   * @param tempF
   * @param pressurePa
   * @param pressureInHg
   * @param altitudeM
   */
  final case class IndoorBmp280(
                           tempC: Option[Double] = None,
                           tempF: Option[Double] = None,
                           pressurePa: Option[Double] = None,
                           pressureInHg: Option[Double] = None,
                           altitudeM: Option[Double] = None
                         ) {
     def toInfluxString(host: String, timestamp: Long): Option[String] = {
       Some(s"""${IndoorBmp280.this.getClass.getSimpleName},host=${host},sensor=${IndoorBmp280.this.getClass.getSimpleName} """ + List(
         altitudeM match {
           case Some(i) => "altitudeM=" + i.toString
           case None => ""
         },
         pressureInHg match {
           case Some(i) => "pressureInHg=" + i.toString
           case None => ""
         },
         pressurePa match {
           case Some(i) => "pressurePa=" + i.toString
           case None => ""
         },
         tempC match {
           case Some(i) => "tempC=" + i.toString
           case None => ""
         },
         tempF match {
           case Some(i) => "tempF=" + i.toString
           case None => ""
         }
       ).mkString(",") + " " + timestamp.toString + "0000")
     }
  }

  final case class IndoorBme280(
                           tempC: Option[Double] = None,
                           tempF: Option[Double] = None,
                           pressurePa: Option[Double] = None,
                           pressureInHg: Option[Double] = None,
                           altitudeM: Option[Double] = None
                         ) {
    def toInfluxString(host: String, timestamp: Long): Option[String] = {
      Some(s"""${IndoorBme280.this.getClass.getSimpleName},host=${host},sensor=${IndoorBme280.this.getClass.getSimpleName} """ + List(
          altitudeM match {
            case Some(i) => "altitudeM=" + i.toString
            case None => ""
          },
          pressureInHg match {
            case Some(i) => "pressureInHg=" + i.toString
            case None => ""
          },
          pressurePa match {
            case Some(i) => "pressurePa=" + i.toString
            case None => ""
          },
          tempC match {
            case Some(i) => "tempC=" + i.toString
            case None => ""
          },
          tempF match {
            case Some(i) => "tempF=" + i.toString
            case None => ""
          }
      ).mkString(",") + " " + timestamp.toString + "0000")
    }
  }

  final case class IndoorBme680(
                           tempC: Option[Double] = None,
                           pressurePa: Option[Double] = None,
                           altitudeMeters: Option[Double] = None,
                           gasOhms: Option[Int] = None,
                           pressureInHg: Option[Double] = None,
                           tempF: Option[Double] = None,
                           humidity: Option[Double] = None
                         ){
    def toInfluxString(host: String, timestamp: Long): Option[String] = {
      Some(s"""${IndoorBme680.this.getClass.getSimpleName},host=${host},sensor=${IndoorBme680.this.getClass.getSimpleName} """ + List(
        tempC match {
          case Some(i) => "tempC=" + i.toString
          case None => ""
        },
        pressurePa match {
          case Some(i) => "pressurePa=" + i.toString
          case None => ""
        },
        altitudeMeters match {
          case Some(i) => "altitudeMeters=" + i.toString
          case None => ""
        },
        gasOhms match {
          case Some(i) => "gasHoms=" + i.toString
          case None => ""
        },
        pressureInHg match {
          case Some(i) => "pressureInHg=" + i.toString
          case None => ""
        },
        tempF match {
          case Some(i) => "tempF=" + i.toString
          case None => ""
        },
        humidity match {
          case Some(i) => "humidity=" + i.toString
          case None => ""
        }
      ).mkString(",") + " " + timestamp.toString + "0000")
    }
  }

  final case class Si1145(
                           Vis: Option[Int] = None,
                           IR: Option[Int] = None,
                           UV: Option[Double] = None
                         ){
    def toInfluxString(host: String, timestamp: Long): Option[String] = {
      Some(s"""${Si1145.this.getClass.getSimpleName},host=${host},sensor=${Si1145.this.getClass.getSimpleName} """ + List(
        Vis match {
          case Some(i) => "Vis=" + i.toString
          case None => ""
        },
        IR match {
          case Some(i) => "IR=" + i.toString
          case None => ""
        },
        UV match {
          case Some(i) => "UV=" + i.toString
          case None => ""
        }
      ).mkString(",") + " " + timestamp.toString + "0000")
    }
  }

  final case class Ccs811(
                           co2: Option[Int] = None,
                           voc: Option[Int] = None
                         ){
    def toInfluxString(host: String, timestamp: Long): Option[String] = {
      Some(s"""${Ccs811.this.getClass.getSimpleName},host=${host},sensor=${Ccs811.this.getClass.getSimpleName} """ + List(
        co2 match {
          case Some(i) => "co2=" + i.toString
          case None => ""
        },
        voc match {
          case Some(i) => "voc=" + i.toString
          case None => ""
        }
      ).mkString(",") + " " + timestamp.toString + "0000")
    }
  }

  final case class Sgp30(
                          TVOCPPB: Option[Int] = None,
                          eCO2PPM: Option[Int] = None
                        ){
    def toInfluxString(host: String, timestamp: Long): Option[String] = {
      Some(s"""${Sgp30.this.getClass.getSimpleName},host=${host},sensor=${Sgp30.this.getClass.getSimpleName} """ + List(
        TVOCPPB match {
          case Some(i) => "TVOCPPB=" + i.toString
          case None => ""
        },
        eCO2PPM match {
          case Some(i) => "eCO2PPM=" + i.toString
          case None => ""
        }
      ).mkString(",") + " " + timestamp.toString + "0000")
    }
  }

  final case class Tsl2561(
                            lux: Option[Double] = None
                          ){
    def toInfluxString(host: String, timestamp: Long): Option[String] = {
      Some(s"""${Tsl2561.this.getClass.getSimpleName},host=${host},sensor=${Tsl2561.this.getClass.getSimpleName} """ + List(
        lux match {
          case Some(i) => "lux=" + i.toString
          case None => ""
        }
      ).mkString(",") + " " + timestamp.toString + "0000")
    }
  }

  final case class Tsl2591(
                            Vis: Option[Int] = None,
                            IR: Option[Int] = None
                          ){
    def toInfluxString(host: String, timestamp: Long): Option[String] = {
      Some(s"""${Tsl2591.this.getClass.getSimpleName},host=${host},sensor=${Tsl2591.this.getClass.getSimpleName} """ + List(
        Vis match {
          case Some(i) => "Vis=" + i.toString
          case None => ""
        },
        IR match {
          case Some(i) => "IR=" + i.toString
          case None => ""
        },
      ).mkString(",") + " " + timestamp.toString + "0000")
    }
  }

}