package flix.deviceregistry.io.swagger.client.model

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsonFormat}

trait DeviceJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val deviceInfoFormat: JsonFormat[DeviceInformation] = jsonFormat6(DeviceInformation)
}

final case class DeviceInformation(deviceId: Option[Long],
                                   deviceName: String,
                                   deviceType: String,
                                   userName: Option[String],
                                   password: Option[String],
                                   ipAddress: Option[String]
                                  )

