
package flix.deviceregistry.connection

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.stream.ActorMaterializer
import flix.deviceregistry.io.swagger.client.model.{DeviceInformation, DeviceJsonSupport}
import flix.deviceregistry.{AppLogging, Registry, RequestHandler}
import spray.json.JsonParser.ParsingException
import spray.json._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Web server configuration for Instance Registry API.
  */
class Server(handler: RequestHandler) extends HttpApp
  with DeviceJsonSupport
  with AppLogging {

  implicit val system: ActorSystem = Registry.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher


  override def routes: server.Route = {
    apiRoutes
  }

  // scalastyle:off method.length

  //Routes that map http endpoints to methods in this object
  def apiRoutes: server.Route =

  /** **************BASIC OPERATIONS ****************/
    pathPrefix("devices") {
      pathEnd {
        allDevices()
      } ~
        path("add") {
          entity(as[String]) {
            jsonString => addDevice(jsonString)
          }
        } ~
        path("update") {
          entity(as[String]) {
            jsonString => updateDevice(jsonString)
          }
        } ~
        pathPrefix(LongNumber) { Id =>
          pathEnd {
            getDevice(Id)
          } ~
            path("delete") {
              removeDevice(Id)
            }
        }
    }

  /**
    * Creates a new device.
    *
    * @param DeviceString Json object describing the device
    * @return
    */
  def addDevice(DeviceString: String): server.Route = Route.seal {

    post {
      log.debug(s"POST /devices/add has been called, parameter is: $DeviceString")
      try {
        val paramInstance: DeviceInformation = DeviceString.parseJson.convertTo[DeviceInformation](deviceInfoFormat)
        handler.handleAddDevice(paramInstance) match {
          case Success(deviceId) =>
            complete {
              deviceId.toString
            }
          case Failure(ex) =>
            log.error(ex, "Failed to handle create device request.")
            complete(HttpResponse(StatusCodes.BadRequest, entity = "Device could not be added."))
        }
      } catch {
        case dx: DeserializationException =>
          log.error(dx, "Deserialization exception")
          complete(HttpResponse(StatusCodes.BadRequest, entity = s"Could not deserialize parameter with message ${dx.getMessage}."))
        case px: ParsingException =>
          log.error(px, "Failed to parse JSON while registering")
          complete(HttpResponse(StatusCodes.BadRequest, entity = s"Failed to parse JSON entity with message ${px.getMessage}"))
        case x: Exception =>
          log.error(x, "Uncaught exception while deserializing.")
          complete(HttpResponse(StatusCodes.InternalServerError, entity = "An internal server error occurred."))
      }
    }
  }

  def updateDevice(DeviceString: String): server.Route = Route.seal {

    put {
      log.debug(s"PUT /devices/update has been called, parameter is: $DeviceString")
      try {
        val paramInstance: DeviceInformation = DeviceString.parseJson.convertTo[DeviceInformation](deviceInfoFormat)
        handler.handleUpdateDevice(paramInstance) match {
          case Success(deviceId) =>
            complete {
              deviceId.toString
            }
          case Failure(ex) =>
            log.error(ex, "Failed to handle update device request.")
            complete(HttpResponse(StatusCodes.BadRequest, entity = "Device could not be updated."))
        }
      } catch {
        case dx: DeserializationException =>
          log.error(dx, "Deserialization exception")
          complete(HttpResponse(StatusCodes.BadRequest, entity = s"Could not deserialize parameter with message ${dx.getMessage}."))
        case px: ParsingException =>
          log.error(px, "Failed to parse JSON while registering")
          complete(HttpResponse(StatusCodes.BadRequest, entity = s"Failed to parse JSON entity with message ${px.getMessage}"))
        case x: Exception =>
          log.error(x, "Uncaught exception while deserializing.")
          complete(HttpResponse(StatusCodes.InternalServerError, entity = "An internal server error occurred."))
      }
    }
  }


  /**
    * Return the list of all Devices (so the resulting call is
    * * /devices)
    *
    * @return
    */
  def allDevices(): server.Route = Route.seal {
    get {
      log.info(handler.getAllDevices().toString())
      complete {
        handler.getAllDevices().toList
      }
    }

  }

  /**
    * Returns an device with the specified id. Id is passed as query argument named 'Id'
    *
    * @param id id of the device to retrieve
    * @return
    */
  def getDevice(id: Long): server.Route = Route.seal {
    get {
      log.debug(s"GET /devices/$id has been called")

      val device = handler.getDevice(id)

      if (device.isDefined) {
        complete(device.get.toJson(deviceInfoFormat))
      } else {
        complete {
          HttpResponse(StatusCodes.NotFound, entity = s"Device id was not found on the server.")
        }
      }
    }
  }

  /**
    * Remove an device with the specified id. Id is passed as query argument named 'Id' (so the resulting call is
    * /device/remove/1)
    *
    * @param id id of the device to remove
    * @return
    */
  def removeDevice(id: Long): server.Route = Route.seal {


    post {
      log.debug(s"POST /devices/delete/$id has been called")
      try {

        handler.handleRemoveDevice(id) match {
          case Success(_) =>
            complete(HttpResponse(StatusCodes.Accepted, entity = "device successfully deleted."))
          case Failure(ex) =>
            log.error(ex, "Failed to handle delete device.")
            complete(HttpResponse(StatusCodes.BadRequest, entity = "device id does not exist."))
        }
      } catch {
        case dx: DeserializationException =>
          log.error(dx, "Deserialization exception")
          complete(HttpResponse(StatusCodes.BadRequest, entity = s"Could not deserialize parameter with message ${dx.getMessage}."))
        case px: ParsingException =>
          log.error(px, "Failed to parse JSON while registering")
          complete(HttpResponse(StatusCodes.BadRequest, entity = s"Failed to parse JSON entity with message ${px.getMessage}"))
        case x: Exception =>
          log.error(x, "Uncaught exception while deserializing.")
          complete(HttpResponse(StatusCodes.InternalServerError, entity = "An internal server error occurred."))
      }
    }

  }
}

