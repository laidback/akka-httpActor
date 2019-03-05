package com.example

import akka.Done
import akka.actor.{ ActorSystem, Props }
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{ delete, get, post }
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.pattern.ask
import akka.routing.RoundRobinPool

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

trait DocumentRoute extends DocumentProtocol {

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem
  implicit val materializer: ActorMaterializer

  lazy val log = Logging(system, classOf[DocumentRoute])

  // Required by the `ask` (?) method below
  // usually we'd obtain the timeout from the system's configuration
  implicit lazy val timeout = Timeout(5.seconds)

  def documentRoute: Route = Route {
    import DocumentManager._
    //val documentManager: ActorRef = system.actorOf(Props(new DocumentManager()))
    val documentManager = system.actorOf(RoundRobinPool(5)
      .props(Props[DocumentManager]), name = "documentManager")
    log.debug(s"document manager: $documentManager")

    implicit val ec = system.dispatcher

    pathPrefix("docs") {
      concat(
        pathEnd {
          concat(
            get {
              val docs = (documentManager ? ListDocuments).mapTo[List[Document]]
              onComplete(docs) { docs =>
                log.debug(s"GET docs: $docs")
                docs match {
                  case Success(v) => complete(v)
                  case Failure(e) => complete(StatusCodes.NotFound)
                }
              }
            },
            post {
              entity(as[Document]) { doc =>
                val created = (documentManager ? CreateDocument(doc)).mapTo[Done]
                onComplete(created) { created =>
                  log.debug(s"POST created, doc: $created, $doc")
                  created match {
                    case Success(v) => complete(v)
                    case Failure(e) => complete(StatusCodes.InternalServerError)
                  }
                }
              }
            })
        },
        path(IntNumber) { id =>
          concat(
            get {
              val doc = (documentManager ? GetDocument(id)).mapTo[Option[Document]]
              onComplete(doc) { doc =>
                log.debug(s"GET doc: $doc")
                doc match {
                  case Success(v) => complete(v)
                  case Failure(e) => complete(StatusCodes.NotFound)
                }
              }
            },
            delete {
              val deleted: Future[Done] = (documentManager ? DeleteDocument(1)).mapTo[Done]
              onComplete(deleted) { deleted =>
                log.debug(s"DELETE id: $deleted")
                deleted match {
                  case Success(v) => complete(StatusCodes.OK)
                  case Failure(e) => complete(StatusCodes.NotModified)
                }
              }
            })
        })
    }
  }
}
