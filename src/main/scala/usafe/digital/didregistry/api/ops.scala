package usafe.digital.didregistry.api

import cats.syntax.either._
import cats.syntax.option._
import cats.syntax.show._
import org.http4s.dsl.impl.QueryParamDecoderMatcher
import usafe.digital.didregistry.DidParser
import usafe.digital.didregistry.types.{Did, DidDocument, DidDocumentValidationFault, DidValidationFault, ParsedDid, RawDid}
import usafe.digital.didregistry.implicits.showDid

object ops {

  import implicits.didQueryDecoder

  object DidSegment {
    def unapply(pathSegment: String): Option[RawDid] = RawDid(pathSegment).some
  }

  def parseDid(raw: RawDid): ParsedDid =
    new DidParser(raw.did).InputLine.run().toEither.leftMap(e => DidValidationFault(e.getMessage))

  def validateDidDocument(didDocument: DidDocument): Either[DidDocumentValidationFault, DidDocument] = {
    val dups = didDocument.publicKey.flatMap {
        _.toList.groupBy(_.id).view.find { case(_, l) => l.size > 1 }
    }
    dups.fold[Either[DidDocumentValidationFault, DidDocument]](didDocument.asRight) { case (d, _) =>
      DidDocumentValidationFault(show"Duplicate public key ID in $d").asLeft
    }
  }

  object CreatorQueryParameterMatcher extends QueryParamDecoderMatcher[Did]("creator")

}

