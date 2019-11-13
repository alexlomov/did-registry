package usafe.digital.didregistry.api

import cats.Show
import cats.effect.Sync
import cats.syntax.all._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes, MediaType}
import usafe.digital.didregistry.api.ops.{CreatorQueryParameterMatcher, DidSegment, parseDid}
import usafe.digital.didregistry.storage.types.{DocumentExists, DocumentNotFound}
import usafe.digital.didregistry.types.{Did, DidDocument, DidDocumentValidationFault, DidValidationFault}

private[api] class Endpoint[F[_]](
  implicit F: Sync[F],
  didDocumentEntityEncoder: EntityEncoder[F, DidDocument],
  didDocumentArrayEntityEncoder: EntityEncoder[F, List[DidDocument]],
  didDocumentEntityDecoder: EntityDecoder[F, DidDocument],
  showDid: Show[Did]
) extends Http4sDsl[F] {

  private val DID_DOCUMENTS = "did-documents"

  def findDocuments(
    findFn: Did => F[DidDocument]
  ): HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / DID_DOCUMENTS :? CreatorQueryParameterMatcher(cre) =>
      for {
        found <- findFn(cre).attempt
        resp <- found match  {
          case Right(dd) =>
            Ok(List(dd))
          case Left(DidValidationFault(m)) =>
            BadRequest(m)
          case Left(DocumentNotFound(_)) =>
            NotFound()
          case Left(e) =>
            InternalServerError(e.getClass.getName + " " + e.getMessage)
        }
      } yield resp
  }

  def getDidDocument(
    getFn: Did => F[DidDocument]
  ) : HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / DID_DOCUMENTS / DidSegment(rawDid) =>
      val did = parseDid(rawDid)
      for {
        d <- F.fromEither(did)
        found <- getFn(d).attempt
        resp <- found match {
          case Right(dd) =>
            Ok(dd)
          case Left(DidValidationFault(m)) =>
            BadRequest(m)
          case Left(DocumentNotFound(_)) =>
            NotFound()
          case Left(e) =>
            InternalServerError(e.getClass.getName + " " + e.getMessage)
        }
      } yield resp
  }

  def postDidDocument(
    postFn: DidDocument => F[Unit]
  ): HttpRoutes[F] = HttpRoutes.of {
    case req @ POST -> Root / DID_DOCUMENTS
      if req.contentType.fold(false) { _.mediaType == MediaType.application.json } =>
      for {
        dd <- req.as[DidDocument]
        posted <- postFn(dd).attempt
        resp <- posted match {
          case Right(_) =>
            Created().map(
              _.withHeaders(Location(req.uri / dd.id.show))
            )
          case Left(DidDocumentValidationFault(m)) =>
            BadRequest(m)
          case Left(DocumentExists(_)) =>
            Conflict()
          case Left(e) =>
            InternalServerError(e.getMessage)
        }
      } yield resp
    case POST -> Root / DID_DOCUMENTS =>
      UnsupportedMediaType()
  }
}

object Endpoint {
  import implicits._
  import usafe.digital.didregistry.implicits.showDid
  def apply[F[_]: Sync]: Endpoint[F] = new Endpoint[F]
}

