package com.example

import akka.Done
import akka.actor.{ ActorRef, ActorSystem, Props, Status }
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{ delete, get, post }
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.stream.ActorMaterializer
import akka.stream.alpakka.slick.scaladsl.{ Slick, SlickSession }
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.Timeout
import akka.pattern.ask

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

trait DocumentRoute extends SprayJsonSupport {

  // we leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val dbSession: SlickSession

  lazy val log = Logging(system, classOf[DocumentRoute])

  // Required by the `ask` (?) method below
  // usually we'd obtain the timeout from the system's configuration
  implicit lazy val timeout = Timeout(5.seconds)

  def documentRoute: Route = Route {
    import DocumentManager._
    import DocumentProtocol._
    val documentManager: ActorRef = system.actorOf(Props(new DocumentManager()))

    implicit val ec = system.dispatcher

    pathPrefix("docs") {
      concat(
        pathEnd {
          concat(
            get {
              val docs: Future[List[Document]] = (documentManager ? ListDocuments).mapTo[List[Document]]
              onComplete(docs) {
                case Success(v) => complete(StatusCodes.OK, v)
                case Failure(e) => complete(StatusCodes.InternalServerError, e)
              }
            },
            post {
              entity(as[Document]) { doc =>
                val created: Future[Done] = (documentManager ? CreateDocument(doc)).mapTo[Done]
                complete(created)
              }
            })
        },
        path(IntNumber) { id =>
          concat(
            get {
              val doc: Future[Document] = (documentManager ? GetDocument(id)).mapTo[Document]
              onComplete(doc) {
                case Success(v) => complete("uno")
                case Failure(e) => complete("error")
              }
            },
            delete {
              val deleted: Future[Done] = (documentManager ? DeleteDocument(1)).mapTo[Done]
              complete(deleted)
            })
        })
    }
  }
}
