package flix.deviceregistry

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import flix.deviceregistry.connection.Server
import flix.deviceregistry.daos.{DatabaseDeviceDAO, DeviceDAO, InMemoryDeviceDAO}

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

object Registry extends AppLogging {
  implicit val system: ActorSystem = ActorSystem("device-management")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher


  val configuration = new Configuration()

  private val deviceDao: DeviceDAO = {
    if (configuration.useInMemoryDB) {
      new InMemoryDeviceDAO(configuration)
    } else {
      new DatabaseDeviceDAO(configuration)
    }
  }
  private val requestHandler = new RequestHandler(deviceDao)

  private val server: Server = new Server(requestHandler)


  def main(args: Array[String]): Unit = {
    requestHandler.initialize()
    server.startServer(configuration.bindHost, configuration.bindPort)
    system.terminate()
  }
}
