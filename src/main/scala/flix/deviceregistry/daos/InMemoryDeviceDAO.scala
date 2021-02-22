package flix.deviceregistry.daos

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import flix.deviceregistry.io.swagger.client.model.DeviceInformation
import flix.deviceregistry.{AppLogging, Configuration, Registry}

import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class InMemoryDeviceDAO(configuration: Configuration) extends DeviceDAO with AppLogging {
  implicit val system: ActorSystem = Registry.system
  implicit val materializer: ActorMaterializer = Registry.materializer
  implicit val ec: ExecutionContext = system.dispatcher

  private val deviceInfo: mutable.Set[DeviceInformation] = new mutable.HashSet[DeviceInformation]()


  override def addDevice(deviceInformation: DeviceInformation): Try[Long] = {

    val id = nextId()
    val newDevice = DeviceInformation(Some(id), deviceInformation.deviceName, deviceInformation.deviceType, deviceInformation.userName, deviceInformation.password, deviceInformation.ipAddress)
    deviceInfo.add(newDevice)

    log.info(s"Added device ${newDevice.deviceName} with id ${newDevice.deviceId.get} to database.")
    Success(id)
  }

  private def nextId(): Long = {
    if (deviceInfo.isEmpty) {
      0L
    } else {
      (deviceInfo.map(i => i.deviceId.getOrElse(0L)) max) + 1L
    }
  }

  override def updateDevice(deviceInformation: DeviceInformation): Try[Long] = {

    val deviceId = deviceInformation.deviceId.getOrElse(0L)

    removeDevice(deviceId)
    val updatedDevice = DeviceInformation(Some(deviceId), deviceInformation.deviceName, deviceInformation.deviceType, deviceInformation.userName, deviceInformation.password, deviceInformation.ipAddress)
    deviceInfo.add(updatedDevice)

    log.info(s"Updated device ${updatedDevice.deviceName} with id ${updatedDevice.deviceId.get} to database.")
    Success(deviceId)
  }

  override def removeDevice(id: Long): Try[Unit] = {
    if (hasDeviceWithId(id)) {
      deviceInfo.remove(deviceInfo.find(i => i.deviceId.get == id).get)
      Success(log.info(s"Successfully removed device with id $id."))
    } else {
      val msg = s"Cannot remove device with id $id, that id is not present."
      log.warning(msg)
      Failure(new RuntimeException(msg))
    }
  }

  override def getDeviceWithId(id: Long): Option[DeviceInformation] = {
    if (hasDeviceWithId(id)) {
      val query = deviceInfo filter { i => i.deviceId.get == id }
      val result = query.iterator.next()
      Some(dataToObjectDevice(result))
    } else {
      None
    }
  }

  override def hasDeviceWithId(id: Long): Boolean = {
    val query = deviceInfo filter { i => i.deviceId.get == id }
    query.nonEmpty
  }

  override def getAllDevices(): List[DeviceInformation] = {
    List() ++ deviceInfo map dataToObjectDevice
  }

  private def dataToObjectDevice(deviceInformation: DeviceInformation): DeviceInformation = {

    DeviceInformation(deviceInformation.deviceId, deviceInformation.deviceName, deviceInformation.deviceType, deviceInformation.userName, deviceInformation.password, deviceInformation.ipAddress)
  }

  override def initialize(): Unit = {
    log.info("Initializing InMemory DAO...")
    clearData()
    log.info("Successfully initialized InMemory DAO.")

  }

  override def shutdown(): Unit = {
    log.info("Shutting down InMemory DAO...")
    clearData()
    log.info("Shutdown complete InMemory DAO.")
  }

  private[daos] def clearData(): Unit = {
    deviceInfo.clear()
  }
}
