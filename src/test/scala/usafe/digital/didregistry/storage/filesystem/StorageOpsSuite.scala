package usafe.digital.didregistry.storage.filesystem

import java.nio.file.{Files, Paths}

import cats.effect.SyncIO
import org.scalatest.{BeforeAndAfterAll, FunSpecLike, Matchers}
import usafe.digital.didregistry.storage.data.Doc

class StorageOpsSuite extends FunSpecLike with Matchers with BeforeAndAfterAll {

  val id: String = "123456789abcdefghi"

  describe("Storage Operations Suite") {
    it("can load the document it just wrote") {

      val d = for {
        ops <- StorageOps[SyncIO](sys.env.getOrElse("TMPDIR", "."))
        _ <- ops.storeDidDocument(Doc)
        l <- ops.loadDidDocument(Doc.id)
      } yield l

      d.unsafeRunSync shouldEqual Doc


    }
  }

  override def afterAll(): Unit =
    try { Files.delete(Paths.get(sys.env("TMPDIR") + s"/$id.did")) } catch { case _: Throwable => }

}
