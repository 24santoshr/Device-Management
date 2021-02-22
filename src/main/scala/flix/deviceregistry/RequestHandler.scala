
package flix.deviceregistry


import akka.actor._
import akka.stream.{ActorMaterializer, Materializer}
import flix.deviceregistry.daos.DeviceDAO
import flix.deviceregistry.io.swagger.client.model.DeviceInformation

import scala.concurrent.ExecutionContext
import scala.language.postfixOps
import scala.util.{Failure, Success, Try}


class RequestHandler(deviceDao: DeviceDAO) extends AppLogging {


  implicit val system: ActorSystem = Registry.system
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher


  def initialize(): Unit = {
    log.info("Initializing request handler...")
    deviceDao.initialize()
    log.info("Done initializing request handler.")
  }

  /**
    * Add device to device database
    *
    * @param device The device to add
    * @return Id assigned to that device
    */
  def handleAddDevice(device: DeviceInformation): Try[Long] = {

    val noIdDevice = DeviceInformation(deviceId = None, deviceName = device.deviceName, deviceType = device.deviceType, userName = device.userName, password = device.password, ipAddress = device.ipAddress)

    deviceDao.addDevice(noIdDevice) match {
      case Success(deviceId) =>
        log.info(s"Successfully handled create device request")
        Success(deviceId)
      case Failure(x) => Failure(x)
    }
  }

  /**
    * update device to device database
    *
    * @param device The device to be updated
    * @return Assigned id to that device
    */

  def handleUpdateDevice(device: DeviceInformation): Try[Long] = {

    val IdDevice = DeviceInformation(deviceId = device.deviceId, deviceName = device.deviceName, deviceType = device.deviceType, userName = device.userName, password = device.password, ipAddress = device.ipAddress)

    deviceDao.updateDevice(IdDevice) match {
      case Success(deviceId) =>
        log.info(s"Successfully handled update device request")
        Success(deviceId)
      case Failure(x) => Failure(x)
    }
  }

  /**
    * Remove a device with id
    *
    * @param id takes id of the device to be deleted
    * @return
    */
  def handleRemoveDevice(id: Long): Try[Long] = {

    deviceDao.removeDevice(id) match {
      case Success(_) =>
        log.info(s"Successfully handled remove device request")
        Success(id)
      case Failure(x) => Failure(x)
    }
  }

  /**
    * Get a device with id
    *
    * @param id takes id of the device to be fetched
    * @return
    */
  def getDevice(id: Long): Option[DeviceInformation] = {
    deviceDao.getDeviceWithId(id)
  }

  /**
    * Get all devices
    *
    * @return
    */
  def getAllDevices(): List[DeviceInformation] = {
    deviceDao.getAllDevices()
  }
}

