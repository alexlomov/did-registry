package usafe.digital.didregistry.storage.filesystem

import java.io.{File, FileInputStream, FileNotFoundException}
import java.nio.file.{Files, Path, Paths}

import cats.effect.{ExitCase, Resource, Sync}
import cats.syntax.applicativeError._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import usafe.digital.didregistry.storage.types.{DocumentExists, DocumentNotFound, StorageInitializationError}
import usafe.digital.didregistry.types.{Did, DidDocument}

import scala.io.Codec


final class StorageOps[F[_]]private[didregistry](workDir: String)
  (
    implicit F: Sync[F],
    didDocumentEncoder: Encoder[DidDocument],
    didDocumentDecoder: Decoder[DidDocument]
  ) {

  private val wd: F[File] = {
    val h = new java.io.File(workDir)
    if (h.exists() && !h.isDirectory)
      F.raiseError[File](StorageInitializationError(s"$workDir must be a directory"))
    else
      F.delay(h.mkdir()).as(h)

  }

  private implicit val codec: Codec = scala.io.Codec.UTF8

  def storeDidDocument(doc: DidDocument): F[Unit] = for {
    d <- wd
    fs = d.listFiles()
    f <- if (fs.exists(_.getName == doc.id.methodSpecificId.value))
      F.raiseError[Path](DocumentExists(doc.id.methodSpecificId))
    else
      F.pure(
        Files.createFile(Paths.get(d.getAbsolutePath, s"${ doc.id.methodSpecificId.value }.did"))
      )
    _ <- F.delay(Files.write(f, doc.asJson.noSpaces.getBytes))
  } yield ()

  def loadDidDocument(did: Did): F[DidDocument] = {

    val fd = Resource.makeCase(
      wd.flatMap { d =>
        F.delay {
          new FileInputStream(
            Paths.get(d.getAbsolutePath, s"${ did.methodSpecificId.value }.did").toFile
          )
        }
      }
    ) {
      case (s, ExitCase.Error(_: FileNotFoundException)) =>
        F.delay(s.close()) *> F.raiseError[Unit](DocumentNotFound(did.methodSpecificId))
      case (s, ExitCase.Error(e)) =>
        F.delay { s.close() } *> F.raiseError[Unit](e)
      case (s, _) =>
        F.delay(s.close())

    }
    fd.use { is =>
      F.delay(scala.io.Source.fromInputStream(is).mkString).flatMap { s =>
        F.fromEither {
          decode[DidDocument](s)
        }
      }
    }.attempt.flatMap {
      case Left(_: FileNotFoundException) =>
        F.raiseError[DidDocument](DocumentNotFound(did.methodSpecificId))
      case Right(d) =>
        F.pure(d)
      case Left(e) =>
        F.raiseError[DidDocument](e)
    }

  }
}

object StorageOps {
  import usafe.digital.didregistry.implicits._
  def apply[F[_]: Sync](workDir: String): F[StorageOps[F]] = {
    val n = new StorageOps[F](workDir)
    n.wd.as(n)
  }
}
