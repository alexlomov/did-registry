package usafe.digital.didregistry.storage.jdbc

import cats.effect.{Blocker, ContextShift, IO}
import cats.syntax.apply._
import cats.syntax.option._
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import doobie.implicits._
import doobie.util.{Colors, ExecutionContexts}
import doobie.util.transactor.Transactor
import org.http4s.Uri
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import usafe.digital.didregistry.config.types.{DriverInfo, JdbcConnection}

import scala.concurrent.duration._
import usafe.digital.didregistry.types.Did
import usafe.digital.didregistry.storage.data.{Doc, DocDuplicatePublicKeyId}

class StorageOpsSuite extends FunSuite with Matchers with doobie.scalatest.IOChecker with ForAllTestContainer with BeforeAndAfterAll {


  override val container: PostgreSQLContainer = PostgreSQLContainer(
    databaseName = "registry",
    username = "postgres",
    password = "postgres"
  )


  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)
  override val colors = Colors.Ansi
  override lazy val transactor = Transactor.fromDriverManager[IO](
    container.driverClassName, container.jdbcUrl, container.username, container.password,
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )

  lazy val q: StorageOps[IO] = StorageOps[IO](
    JdbcConnection(
      DriverInfo(container.driverClassName),
      Uri.unsafeFromString(container.jdbcUrl),
      container.username.some,
      container.password.some
    )
  )

  test("database is ready") {
    val ddl1 = sql"""
         create table public_keys2doc(
           pubk_id varchar(1024) primary key,
           did_doc_id varchar(1024) not null
         )
         """.update.run

    val ddl2 = sql"""
         create table did_doc(
           doc_id varchar(1024) primary key,
           doc jsonb not null
         )
         """.update.run

    noException should be thrownBy (IO.timer(ExecutionContexts.synchronous).sleep(5.seconds) *> (ddl1, ddl2).mapN(_ + _).transact(transactor)).unsafeRunSync()

  }

  test("insert document public keys DML") {

    val keyId1 = Did.fromStringUnsafe("did:example:123456789abcdefghi#keys-1")
    val docId = Did.fromStringUnsafe("did:example:123456789abcdefghi")

    check(q.SQL.insertDocumentPublicKeys.toUpdate0(keyId1 -> docId))
  }

  test("insert document DML") {
    check(q.SQL.insertDocument.toUpdate0(Doc.id -> Doc))
  }

  test("select document DML") {
    val docId = Did.fromStringUnsafe("did:example:123456789abcdefghi")
    check(q.SQL.selectDocumentById.toQuery0(docId))
  }

  test("select document by public key ID DML") {
    val keyId = Did.fromStringUnsafe("did:example:123456789abcdefghi#key-100")
    check(q.SQL.selectDocumentByPublicKeyId.toQuery0(keyId))
  }

  test("store a document") {

    q.store(Doc).attempt.unsafeRunSync() match {
      case Left(e) =>
        alert(e.getMessage)
        fail()
      case Right(_) =>
        succeed
    }
  }

  test("read your writes") {

    q.load(Doc.id).attempt.unsafeRunSync() match {
      case Left(e) =>
        alert(e.getMessage)
        fail(e)
      case Right(d) if d == Doc =>
        succeed
      case Right(x) =>
        fail(s"WAT $x")
    }
  }

  test("find document by any key it holds") {
    val ok = Doc.publicKey.get.forall { pubK =>
      q.findByPublicKeyId(pubK.id).attempt.unsafeRunSync() match {
        case Left(e) =>
          fail(e)
        case Right(d) if d == Doc =>
          true
        case Right(x) =>
          fail(s"Found unexpected document $x")
      }
    }

    ok shouldBe true
  }

  test("no documents with duplicate public key IDs allowed") {
    q.store(DocDuplicatePublicKeyId).attempt.unsafeRunSync() match {
      case Left(e) =>
        info(e.getMessage)
        succeed
      case Right(_) =>
        fail
    }
  }

}
