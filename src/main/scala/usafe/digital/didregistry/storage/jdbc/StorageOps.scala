package usafe.digital.didregistry.storage.jdbc

import cats.syntax.all._
import cats.instances.list._
import cats.instances.int._
import cats.effect.{Async, ContextShift}
import doobie._
import doobie.util.Put._
import doobie.syntax.all._
import doobie.util.transactor.Transactor
import usafe.digital.didregistry.config.types.JdbcConnection
import usafe.digital.didregistry.storage.types.{DocumentNotFound, IntegrityViolationFailure}
import usafe.digital.didregistry.types.{Did, DidDocument}
import usafe.digital.didregistry.implicits.showDid
import scala.jdk.CollectionConverters.MapHasAsJava

final class StorageOps[F[_]: Async: ContextShift] private[jdbc] (jdbcConnection: JdbcConnection)
  (
   implicit writePubKDid: Write[(Did, Did)],
    writeDidDoc: Write[(Did, DidDocument)],
    getDidDocument: Read[DidDocument],
    putDid: Put[Did]
  ) {

  implicit val han: LogHandler = LogHandler.jdkLogHandler

  private val jdbcProps = new java.util.Properties()

  jdbcProps.putAll(
      (Map.from(
        jdbcConnection.user.map { u => "user" -> u }
      ) ++
        Map.from(
          jdbcConnection.password.map { p => "password" -> p }
        )).asJava
  )


  lazy val tx = Transactor.fromDriverManager[F](
    driver = jdbcConnection.driverInfo.value,
    url = jdbcConnection.connectionString.renderString,
    info = jdbcProps
  )


  def store(didDocument: DidDocument): F[Unit] = {
    val keyToDocument: List[(Did, Did)] = didDocument.publicKey.fold[List[(Did, Did)]](Nil) { pks =>
      pks.toList.map(_.id -> didDocument.id)
    }
    (SQL.insertDocumentPublicKeys.updateMany(keyToDocument) *>  SQL.insertDocument.run(didDocument.id -> didDocument))
      .transact(tx).void
  }

  def load(id: Did): F[DidDocument] =
    SQL.selectDocumentById.toQuery0(id).to[List].transact(tx).flatMap {
      case h :: Nil => Async[F].pure(h)
      case Nil => DocumentNotFound(id.methodSpecificId).raiseError
      case m => throw IntegrityViolationFailure(show"$id is duplicate in the store. There are at least ${m.size} records")
    }

  def findByPublicKeyId(publicKeyId: Did): F[DidDocument] =
    SQL.selectDocumentByPublicKeyId.toQuery0(publicKeyId).to[List].transact(tx).flatMap {
      case h :: Nil => Async[F].pure(h)
      case Nil => DocumentNotFound(publicKeyId.methodSpecificId).raiseError
      case m => throw IntegrityViolationFailure(show"${publicKeyId} is duplicate in the store. There are at least ${m.size} records")
    }

  object SQL {
    val insertDocumentPublicKeys: Update[(Did, Did)] =
      Update[(Did, Did)]("insert into public_keys2doc (pubk_id, did_doc_id) values (?, ?)")

    val insertDocument: Update[(Did, DidDocument)] =
      Update[(Did, DidDocument)]("insert into did_doc (doc_id, doc) values (?, ?)")

    val selectDocumentById: Query[Did, DidDocument] =
      Query[Did, DidDocument]("select doc from did_doc where doc_id = ?")

    val selectDocumentByPublicKeyId: Query[Did, DidDocument] = Query[Did, DidDocument](
        """select d.doc
          |from did_doc d
          |join public_keys2doc pk2d on pk2d.did_doc_id = d.doc_id
          |where pk2d.pubk_id = ?""".stripMargin
    )
  }
}

object StorageOps {
  import usafe.digital.didregistry.storage.jdbc.implicits._
  def apply[F[_]: Async: ContextShift](jdbcConnection: JdbcConnection): StorageOps[F] =
    new StorageOps(jdbcConnection)
}
