package usafe.digital.didregistry.storage.jdbc

import cats.syntax.either._
import cats.syntax.show._
import doobie.util.Meta
import io.circe.Json
import io.circe.parser.parse
import io.circe.syntax._
import org.postgresql.util.PGobject
import usafe.digital.didregistry.DidParser
import usafe.digital.didregistry.types.{Did, DidDocument}
import usafe.digital.didregistry.implicits.showDid

object implicits {

  import usafe.digital.didregistry.implicits.{didDocumentEncoder, didDocumentDecoder}

  implicit val jsonMeta: Meta[Json] = Meta.Advanced.other[PGobject]("jsonb")
    .timap[Json](o => parse(o.getValue).leftMap[Json](e => throw e).merge)(j => {
      val o = new PGobject()
      o.setType("jsonb")
      o.setValue(j.noSpaces)
      o
    })

  implicit val didMeta: Meta[Did] = Meta[String]
    .timap(s => new DidParser(s).InputLine.run().toEither.leftMap[Did](e => throw e).merge) (_.show)

  implicit val didDocumentMeta: Meta[DidDocument] = Meta[Json]
    .timap(_.as[DidDocument].leftMap[DidDocument](e => throw e).merge) (_.asJson)

}
