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

import flix.deviceregistry.Configuration
import flix.deviceregistry.io.swagger.client.model.DeviceInformation
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

class DatabaseDeviceDAOTest extends FlatSpec with Matchers with BeforeAndAfterEach {

  val config = new Configuration()
  val dao: DatabaseDeviceDAO = new DatabaseDeviceDAO(config)
  dao.setDatabaseConfiguration("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MYSQL", "", "org.h2.Driver")

  "The Device Dao" must "be able to add a device" in {
    val deviceName = dao.addDevice(buildDeviceInfo(id = 1, deviceName = "test1"))
    assert(deviceName.isSuccess)
  }

  it must "return device with correct id" in {
    val device = dao.getDeviceWithId(1)
    assert(device.isDefined)
    assert(device.get.deviceId.isDefined)
    assert(device.get.deviceId.get == 1)
  }

  it must "return all device" in {
    val idOption = dao.addDevice(buildDeviceInfo(id = 2, deviceName = "test2"))
    assert(idOption.isSuccess)
    assert(dao.getAllDevices().size == 2)
  }

  it must "be able to update a device " in {
    val deviceName = dao.updateDevice(buildDeviceInfo(id = 2, deviceName = "test3"))
    assert(deviceName.isSuccess)
    assert(dao.getAllDevices().size == 2)
  }

  it must "delete device for given id" in {
    val idOption = dao.removeDevice(2)
    assert(idOption.isSuccess)
    assert(dao.getAllDevices().size == 1)
  }

  private def buildDeviceInfo(id: Int, deviceName: String = ""): DeviceInformation = {
    val name = if (deviceName == "") "someDevice" + id else deviceName
    DeviceInformation(Some(id), name, "someType", Some("validUser"), Some("validPassword"), Some("validIpAddress"))
  }
}
