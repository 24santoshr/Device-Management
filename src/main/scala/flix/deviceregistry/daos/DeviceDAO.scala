// Copyright (C) 2018 The Delphi Team.
// See the LICENCE file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
