package usafe.digital.didregistry.proof

import java.time.ZonedDateTime

import cats.syntax.either._
import io.circe.{Decoder, DecodingFailure, Encoder, JsonObject}
import io.circe.syntax._
import usafe.digital.didregistry.proof.types.{Proof, ProofSuiteType, ProofValue, RsaSignature2018, SanitizedProof}
import usafe.digital.didregistry.implicits.didEncoder
import usafe.digital.didregistry.types.Did

object implicits {

  import usafe.digital.didregistry.implicits.didDecoder

  implicit val encodeProofSuiteType: Encoder[ProofSuiteType] = Encoder.encodeString.contramap {
    case RsaSignature2018 => "RsaSignature2018"
  }

  implicit val decodeProofSuiteType: Decoder[ProofSuiteType] = Decoder.instance { c =>
    c.as[String].flatMap {
      case "RsaSignature2018" => RsaSignature2018.asRight[DecodingFailure]
      case x => DecodingFailure(s"Unsupported Proof Suite Type $x", c.history).asLeft[ProofSuiteType]
    }
  }

  implicit val encodeProofValue: Encoder[ProofValue] = Encoder.encodeString.contramap { _.value }

  implicit val decodeProofValue: Decoder[ProofValue] = Decoder.instance { _.as[String].map(ProofValue) }

  implicit val encodeProof: Encoder[Proof] = Encoder.encodeJsonObject.contramapObject { p =>
    JsonObject(
      "type" := p.`type`,
      "creator" := p.creator,
      "created" := p.created,
      "signatureValue" := p.proofValue
    )
  }

  implicit val encodeSanitizedProof: Encoder[SanitizedProof] = Encoder.encodeJsonObject.contramapObject { sp =>
    JsonObject(
      "creator" := sp.creator,
      "created" := sp.created
    )
  }

  implicit val decodeProof: Decoder[Proof] = Decoder.instance { c =>
    for {
      tp <- c.downField("type").as[ProofSuiteType]
      creator <- c.downField("creator").as[Did]
      created <- c.downField("created").as[ZonedDateTime]
      sigVal <- c.downField("signatureValue").as[ProofValue]
    } yield Proof(tp, creator, created, sigVal)
  }

}
