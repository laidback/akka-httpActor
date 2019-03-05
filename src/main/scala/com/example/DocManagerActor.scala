package com.example

import akka.Done
import akka.actor.{ Actor, ActorLogging, Props }
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.stream.ActorMaterializer
import akka.stream.alpakka.slick.scaladsl.{ Slick, SlickSession }
import akka.stream.scaladsl.{ Sink, Source }
import slick.jdbc.GetResult
import spray.json.{ DefaultJsonProtocol, JsonFormat, RootJsonFormat }

case class User(id: Int, name: String)

object UserProtocol extends SprayJsonSupport {
  import DefaultJsonProtocol._
  implicit val userFormat: JsonFormat[User] = jsonFormat2(User)
}

case class Document(id: Int, name: String)

trait DocumentProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val documentFormat: RootJsonFormat[Document] = jsonFormat2(Document)
}

object DocumentManager {
  def props(implicit materializer: ActorMaterializer): Props =
    Props[DocumentManager]

  var documents = Set.empty[Document]

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

class DocumentManager extends Actor with ActorLogging {
  import DocumentManager._
  implicit val materializer = ActorMaterializer()
  implicit val dbSession: SlickSession = SlickSession.forConfig("slick-h2")
  import dbSession.profile.api._

  override def receive: Receive = {

    case ListDocuments =>
      //val docs = documents.toList
      val docs = Slick
        .source(sql"SELECT id, name FROM Document".as[Document])
        .log(s"slick list document")
        .runWith(Sink.seq)

      log.debug(s"list documents: $docs")
      sender() ! docs

    case GetDocument(id) =>
      //val document = documents.find(_.id == id)

      val document = Slick
        .source(sql"SELECT id, name FROM Document WHERE id=$id".as[Document])
        .log(s"slick get document")
        .runWith(Sink.headOption[Document])

      log.debug(s"get document: $document")
      sender() ! document

    case CreateDocument(doc) =>
      //val created = (documents = documents ++ Seq(doc))

      val document = Source
        .single(doc)
        .via(Slick.flow(doc => sqlu"INSERT INTO Document(name) VALUES(${doc.name})"))
        .log(s"slick create document")
        .runWith(Sink.head)

      log.debug(s"create document from doc: $document, $doc")
      sender() ! Done

    case UpdateDocument(doc) =>
      //val updated = documents.find(_.id == doc.id) foreach (document => documents -= document)

      val document = Source
        .single(doc)
        .via(Slick.flow(doc => sqlu"UPDATE Document SET name=${doc.name} WHERE id=${doc.id}"))
        .log(s"slick update document")
        .runWith(Sink.head)

      log.debug(s"update document from doc: $document, $doc")
      sender() ! documents

    case DeleteDocument(id) =>
      //val done = documents.find(_.id == id) foreach (document => documents -= document)

      val document = Source
        .single(id)
        .via(Slick.flow(doc => sqlu"DELETE FROM Document WHERE id=$id"))
        .log(s"slick delete document")
        .runWith(Sink.head)

      log.debug(s"delete document: $document")
      sender() ! Done
  }
}
