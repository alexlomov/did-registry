package usafe.digital.didregistry.api

import cats.Applicative
import cats.effect.Sync
import cats.syntax.either._
import org.http4s.{EntityDecoder, EntityEncoder, ParseFailure, QueryParamDecoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import usafe.digital.didregistry.DidParser
import usafe.digital.didregistry.types.{Did, DidDocument}

object implicits {

  import usafe.digital.didregistry.implicits.{didDocumentEncoder, didDocumentDecoder}

  implicit def didDocumentEntityEncoder[F[_]: Applicative]: EntityEncoder[F, DidDocument] = jsonEncoderOf
  implicit def didDocumentArrayEntityEncoder[F[_]: Applicative]: EntityEncoder[F, List[DidDocument]] = jsonEncoderOf
  implicit def didDocumentEntityDecoder[F[_]: Sync]: EntityDecoder[F, DidDocument] = jsonOf

  implicit val didQueryDecoder: QueryParamDecoder[Did] = QueryParamDecoder[String].emap { v =>
    new DidParser(v).InputLine.run().toEither.leftMap { _ => ParseFailure("Invalid DID format", s"Invalid DID format: $v") }
  }


}
