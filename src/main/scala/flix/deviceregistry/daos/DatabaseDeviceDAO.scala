package flix.deviceregistry.daos

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import flix.deviceregistry.io.swagger.client.model.DeviceInformation
import flix.deviceregistry.{AppLogging, Configuration, Registry}
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.meta.MTable
import slick.lifted.TableQuery

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


class DatabaseDeviceDAO(configuration: Configuration) extends DeviceDAO with AppLogging {

  implicit val system: ActorSystem = Registry.system
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  private val deviceInfo: TableQuery[DeviceInfo] = TableQuery[DeviceInfo]
  private var dbAuth = Database.forURL(configuration.databaseHost + configuration.databaseName,
    driver = configuration.databaseDriver,
    user = configuration.databaseUserName,
    password = configuration.databasePassword)


  override def addDevice(deviceInformation: DeviceInformation): Try[Long] = {
    val id = 0L //Will be set by DB
    val deviceName = deviceInformation.deviceName
    val deviceType = deviceInformation.deviceType
    val userName = deviceInformation.userName
    val password = deviceInformation.password
    val ipAddress = deviceInformation.ipAddress

    val addFuture: Future[Long] = dbAuth.run((deviceInfo returning deviceInfo.map(_.deviceId)) += (id, deviceName, deviceType, userName, password, ipAddress))
    val deviceId = Await.result(addFuture, Duration.Inf)

    log.info(s"Added device ${deviceInformation.deviceName} with id $deviceId to database.")
    Success(deviceId)

  }


  override def updateDevice(deviceInformation: DeviceInformation): Try[Long] = {

    val id = deviceInformation.deviceId.getOrElse(0L)

    val deviceName = deviceInformation.deviceName
    val deviceType = deviceInformation.deviceType
    val userName = deviceInformation.userName
    val password = deviceInformation.password
    val ipAddress = deviceInformation.ipAddress

    val addFuture: Future[Int] = dbAuth.run((deviceInfo.filter(_.deviceId === id).map(x => (x.deviceName, x.deviceType, x.userName, x.password, x.ipAddress)).update(deviceName, deviceType, userName, password, ipAddress)))
    val resultId = Await.result(addFuture, Duration.Inf)

    log.info(s"Updated device ${deviceInformation.deviceName} with id $resultId to database.")
    Success(resultId)

  }

  override def removeDevice(id: Long): Try[Unit] = {
    if (hasDeviceWithId(id)) {
      removeDeviceWithId(id)
      Success(log.info(s"Successfully removed device with id $id."))
    } else {
      val msg = s"Cannot remove device with id $id, that id is not present."
      log.warning(msg)
      Failure(new RuntimeException(msg))
    }
  }

  override def hasDeviceWithId(id: Long): Boolean = {
    Await.result(dbAuth.run(deviceInfo.filter(_.deviceId === id).exists.result), Duration.Inf)
  }

  private def removeDeviceWithId(id: Long): Unit = {
    val q = deviceInfo.filter(_.deviceId === id)
    val action = q.delete
    dbAuth.run(action)
  }

  override def getDeviceWithId(id: Long): Option[DeviceInformation] = {
    if (hasDeviceWithId(id)) {
      val result = Await.result(dbAuth.run(deviceInfo.filter(_.deviceId === id).result.headOption), Duration.Inf)
      Some(dataToObjectDevice(result))
    } else {
      None
    }
  }

  override def getAllDevices(): List[DeviceInformation] = {
    var listDevice = List[DeviceInformation]()
    val hasData = Await.result(dbAuth.run(deviceInfo.exists.result), Duration.Inf)
    if (hasData) {
      val resultAll = Await.result(dbAuth.run(deviceInfo.result), Duration.Inf)
      val listAll = List() ++ resultAll.map(_.value)
      listDevice = listAll.map(c => dataToObjectDevice(Option(c)))
    }
    listDevice
  }

  private def dataToObjectDevice(options: Option[(Long, String, String, Option[String], Option[String], Option[String])]): DeviceInformation = {
    val optionValue = options.get
    DeviceInformation(Some(optionValue._1), optionValue._2, optionValue._3, optionValue._4, optionValue._5, optionValue._6)
  }

  override def shutdown(): Unit = {
    log.info("Shutting down dynamic auth DAO...")
    log.info("Shutdown complete.")
  }

  def setDatabaseConfiguration(databaseHost: String = "",
                               databaseName: String = "",
                               databaseDriver: String = "",
                               databaseUsername: String = "",
                               databasePassword: String = ""): Unit = {
    dbAuth = Database.forURL(databaseHost + databaseName,
      driver = databaseDriver,
      user = databaseUsername,
      password = databasePassword)
    initialize()
  }

  override def initialize(): Unit = {
    if (dbTest()) {
      log.info("Initializing sql auth DAO...")
      val authTables = List(deviceInfo)
      val authExisting = dbAuth.run(MTable.getTables)
      val authCreateAction = authExisting.flatMap(v => {
        val names = v.map(mt => mt.name.name)
        val createIfNotExist = authTables.filter(table =>
          !names.contains(table.baseTableRow.tableName)).map(_.schema.create)
        dbAuth.run(DBIO.sequence(createIfNotExist))
      })
      Await.result(authCreateAction, Duration.Inf)
      log.info("Successfully initialized.")
    } else {
      log.error("Not found any database with the provided settings.")

      val terminationFuture = system.terminate()

      terminationFuture.onComplete {
        sys.exit(0)
      }
    }

  }

  private def dbTest(): Boolean = {
    try {
      val dbTimeoutSeconds = 5
      dbAuth.createSession.conn.isValid(dbTimeoutSeconds)
    } catch {
      case e: Throwable => throw e
    }
  }

}
