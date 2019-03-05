package com.example

//#quick-start-server
import akka.Done

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration.Duration
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.alpakka.slick.javadsl.SlickSession
import akka.stream.alpakka.slick.scaladsl.Slick
import akka.stream.scaladsl.Sink

//#main-class
object DocumentServer extends App with DocumentRoute {

  //#main-class
  //#server-bootstrapping
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  //#server-bootstrapping

  // implicit val dbSession = SlickSession.forConfig("slick-h2")
  // log.debug(s"dbsession: $dbSession")//

  //#main-class
  // from the UserRoutes trait
  lazy val route: Route = documentRoute
  //#main-class

  //#http-server
  Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)
  //#http-server
  //#main-class
}
//#main-class
//#quick-start-server
