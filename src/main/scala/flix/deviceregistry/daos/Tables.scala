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

import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape
import slick.sql.SqlProfile.ColumnOption.NotNull

object StringLengthDefinitions {
  val EnumStringLength: Int = 50
  val HashStringLength: Int = 65
  val NameStringLength: Int = 100
  val UriStringLength: Int = 255
  val LongCompositeStringLenght: Int = 1000
}


class DeviceInfo(tag: Tag) extends Table[(Long, String, String, Option[String], Option[String], Option[String])](tag, "deviceInfo") {
  def * : ProvenShape[(Long, String, String, Option[String], Option[String], Option[String])] = (deviceId, deviceName, deviceType, userName, password, ipAddress)

  def deviceId: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column

  def deviceName: Rep[String] = column[String]("deviceName", O.Length(StringLengthDefinitions.NameStringLength), NotNull)

  def deviceType: Rep[String] = column[String]("deviceType", O.Length(StringLengthDefinitions.NameStringLength), NotNull)

  def userName: Rep[Option[String]] = column[String]("userName", O.Length(StringLengthDefinitions.NameStringLength))

  def password: Rep[Option[String]] = column[String]("password", O.Length(StringLengthDefinitions.NameStringLength))

  def ipAddress: Rep[Option[String]] = column[String]("ipAddress", O.Length(StringLengthDefinitions.HashStringLength))
}