package flix.deviceregistry.connection

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse}
import flix.deviceregistry.AppLogging

import scala.concurrent.{ExecutionContext, Future}

object RestClient extends AppLogging{

  def executePost(requestUri: String)
                 (implicit system: ActorSystem, ec: ExecutionContext): Future[HttpResponse] =
    Http().singleRequest(HttpRequest(HttpMethods.POST, uri = requestUri))

}
