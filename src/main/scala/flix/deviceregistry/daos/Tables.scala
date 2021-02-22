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
