package com.example

import akka.{ Done, actor }
import akka.actor.{ Actor, ActorLogging, Props }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorMaterializer
import akka.stream.alpakka.slick.scaladsl.{ Slick, SlickSession }
import akka.stream.scaladsl.{ Sink, Source }
import slick.jdbc.GetResult
import spray.json.{ DefaultJsonProtocol, JsonFormat }

import scala.concurrent.Future

case class User(id: Int, name: String)

object UserProtocol extends SprayJsonSupport {
  import DefaultJsonProtocol._
  implicit val userFormat: JsonFormat[User] = jsonFormat2(User)
}

final case class Document(id: Int, name: String)

trait DocumentProtocol extends SprayJsonSupport {
  import DefaultJsonProtocol._
  implicit val documentFormat = jsonFormat2(Document)
}

object DocumentManager {
  def props(implicit materializer: ActorMaterializer): Props =
    Props[DocumentManager]

  final case object ListDocuments
  final case class GetDocument(id: Int)
  final case class CreateDocument(doc: Document)
  final case class UpdateDocument(doc: Document)
  final case class DeleteDocument(id: Int)

  final case class DocumentCreated(id: Int)
  final case class DocumentUpdated(id: Int)
  final case class DocumentDeleted(id: Int)

  implicit val getDocumentResult = GetResult(r =>
    Document(r.nextInt, r.nextString))
}

class DocumentManager(implicit materializer: ActorMaterializer)
  extends Actor with ActorLogging {

  import DocumentManager._
  implicit val dbSession: SlickSession = SlickSession.forConfig("slick-h2")
  import dbSession.profile.api._

  var documents = Set.empty[Document]

  override def receive: Receive = {
    case ListDocuments =>
      val documents = Slick.source(sql"SELECT id, name FROM Document;".as[Document]).runWith(Sink.seq)
      sender() ! documents

    case GetDocument(id) =>
      val document = Slick.source(sql"SELECT name FROM Document WHERE id=${id};".as[Document]).runWith(Sink.head)
      sender() ! document

    case CreateDocument(doc) =>
      val created: Future[Done] = Source.single(doc)
        .runWith(Slick.sink(doc => sqlu"INSERT INTO Document VALUES(${doc.name})"))
      sender() ! created

    case UpdateDocument(doc) =>
      val updated: Future[Done] = Source.single(doc)
        .runWith(Slick.sink(doc => sqlu"INSERT INTO Document VALUES(${doc.name})"))
      sender() ! updated

    case DeleteDocument(id) =>
      val deleted = Source.single(s"$id").runWith(
        Slick.sink(id => sqlu"DELETE FROM Document WHERE id=${id};"))
      sender() ! deleted
  }
}

/**
 * Use this with a local Set storage
 * case CreateDocument(doc)
 * documents = documents ++ Set(doc)
 * sender() ! DocumentCreated
 * case UpdateDocument(document) =>
 * documents = Set.empty[Document]
 * sender() ! DocumentUpdated
 * case DeleteDocument(name) =>
 * documents.find(_.name == name) foreach (document => documents -= document)
 * sender() ! DocumentDeleted
 */
