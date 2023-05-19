package usafe.digital.didregistry.api

import cats.Applicative
import cats.effect.Sync
import cats.syntax.either._
import io.circe.Printer
import org.http4s.{EntityDecoder, EntityEncoder, ParseFailure, QueryParamDecoder}
import org.http4s.circe.{jsonEncoderWithPrinterOf, jsonOf}
import usafe.digital.didregistry.DidParser
import usafe.digital.didregistry.types.{Did, DidDocument}

object implicits {

  private val noNullPrinter = Printer.noSpaces.copy(dropNullValues = true)

  import usafe.digital.didregistry.implicits.{didDocumentEncoder, didDocumentDecoder}

  implicit def didDocumentEntityEncoder[F[_]: Applicative]: EntityEncoder[F, DidDocument] =
    jsonEncoderWithPrinterOf(noNullPrinter)
  implicit def didDocumentArrayEntityEncoder[F[_]: Applicative]: EntityEncoder[F, List[DidDocument]] =
    jsonEncoderWithPrinterOf(noNullPrinter)
  implicit def didDocumentEntityDecoder[F[_]: Sync]: EntityDecoder[F, DidDocument] = jsonOf

  implicit val didQueryDecoder: QueryParamDecoder[Did] = QueryParamDecoder[String].emap { v =>
    new DidParser(v).InputLine.run().toEither.leftMap { _ => ParseFailure("Invalid DID format", s"Invalid DID format: $v") }
  }


}
