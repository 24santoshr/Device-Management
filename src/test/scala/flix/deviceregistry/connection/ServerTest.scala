package flix.deviceregistry.connection


import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import flix.deviceregistry.daos.{DeviceDAO, InMemoryDeviceDAO}
import flix.deviceregistry.io.swagger.client.model.{DeviceInformation, DeviceJsonSupport}
import flix.deviceregistry.{Configuration, RequestHandler}
import org.scalatest.{Matchers, WordSpec}
import spray.json._

import scala.util.{Failure, Success, Try}


class ServerTest
  extends WordSpec
    with Matchers
    with ScalatestRouteTest
    with DeviceJsonSupport {

  private val configuration: Configuration = new Configuration()
  private val inMemoryDao: DeviceDAO = new InMemoryDeviceDAO(configuration)
  private val requestHandler: RequestHandler = new RequestHandler(inMemoryDao)
  private val server: Server = new Server(requestHandler)

  //JSON CONSTANTS
  private val validJsonDeviceInfo = DeviceInformation(deviceId = None, deviceName = "validDevice1", deviceType = "mobile",
    userName = Some("validUser1"), password = Some("validUser"),
    ipAddress = None).toJson(deviceInfoFormat
  ).toString

  private val validJsonUpdateDeviceInfo = DeviceInformation(deviceId = Some(1), deviceName = "validDevice1", deviceType = "Laptop",
    userName = Some("validUser1"), password = Some("validPassword"),
    ipAddress = Some("127.0.0.1")).toJson(deviceInfoFormat
  ).toString


  /**
    * Before all tests: Initialize handler and wait for server binding to be ready.
    */
  override def beforeAll(): Unit = {
    requestHandler.initialize()
    inMemoryDao.initialize()
    inMemoryDao.addDevice(DeviceInformation(None, "validDevice2", "Camera", Some("validUser2"), Some("password"), Some("ipAddress")))
  }

  /**
    * After all tests: Unbind the server, shutdown handler and await termination of both actor systems.
    */


  "The Server" should {

    "successfully create device when everything is valid" in {
      Post("/devices/add", HttpEntity(ContentTypes.`application/json`, validJsonDeviceInfo.stripMargin)) ~> Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.OK)
      }
    }

    "successfully update device when everything is valid" in {
      Put("/devices/update", HttpEntity(ContentTypes.`application/json`, validJsonUpdateDeviceInfo.stripMargin)) ~> Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.OK)
      }
    }

    "not create device if request is invalid" in {

      //should use valid request method
      Get("/devices/add") ~> Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.METHOD_NOT_ALLOWED)
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: POST"
      }
    }

    "not update device when method is Invalid" in {
      Post("/devices/update", HttpEntity(ContentTypes.`application/json`, validJsonUpdateDeviceInfo.stripMargin)) ~> Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.METHOD_NOT_ALLOWED)
      }
    }

    "successfully remove device when everything is valid" in {
      inMemoryDao.addDevice(DeviceInformation(None, "validDevice3", "Mobile", Some("validUser3"), Some("password"), Some("ipAddress")))
      Post("/devices/2/delete") ~> Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.ACCEPTED)
      }
    }

    "not remove device if request is invalid" in {
      inMemoryDao.addDevice(DeviceInformation(None, "validDevice4", "Mobile", Some("validUser4"), Some("password"), Some("ipAddress")))

      //should use valid request method
      Get("/devices/4/delete") ~> Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.METHOD_NOT_ALLOWED)
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: POST"
      }
    }

    "successfully get device if device exist" in {
      inMemoryDao.addDevice(DeviceInformation(None, "validDevice5", "Mobile", Some("validUser5"), Some("password"), Some("ipAddress")))
      Get("/devices/1") ~> Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.OK)
      }
    }


    "not get device if request is invalid" in {

      //should use valid request method
      Post("/devices/1") ~> Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.METHOD_NOT_ALLOWED)
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
      }
    }

    "successfully get all devices" in {
      //Valid get instances with no parameter
      Get("/devices") ~> server.routes ~> check {
        assert(status === StatusCodes.OK)
        Try(responseAs[String].parseJson.convertTo[List[DeviceInformation]](listFormat(deviceInfoFormat))) match {
          case Success(listOfDevices) =>
            listOfDevices.size shouldEqual 4
          case Failure(ex) =>
            fail(ex)
        }
      }
    }

    "not get all device if request is invalid" in {


      //should use valid request method
      Post("/devices") ~> Route.seal(server.routes) ~> check {
        assert(status === StatusCodes.METHOD_NOT_ALLOWED)
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
      }
    }
  }
}

