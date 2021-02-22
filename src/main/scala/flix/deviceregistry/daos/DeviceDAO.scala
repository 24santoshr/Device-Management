package flix.deviceregistry.daos

import flix.deviceregistry.io.swagger.client.model.DeviceInformation

import scala.util.Try

trait DeviceDAO {

  /**
    * Add device
    * @return
    */
  def addDevice(deviceInformation : DeviceInformation) : Try[Long]

  /**
    * Update device
    * @return
    */
  def updateDevice(deviceInformation : DeviceInformation) : Try[Long]

  /**
    * Remove device
    * @param id
    * @return
    */
  def removeDevice(id: Long) : Try[Unit]

  /**
    * Initializes the DAO
    */
  def initialize() : Unit

  /**
    * Gets the device with the specified id from the DAO
    * @param id
    * @return
    */
  def getDeviceWithId(id: Long) : Option[DeviceInformation]

  /**
    * Get all devices
    * @return
    */
  def getAllDevices() : List[DeviceInformation]


  /**
    * Checks whether the DAO holds an device with the specified id.
    * @param id
    * @return
    */
  def hasDeviceWithId(id: Long) : Boolean

  /**
    * Shuts the DAO down
    */
  def shutdown(): Unit
}
