package usafe.digital.didregistry.proof

import java.io.{DataInputStream, File, FileInputStream}
import java.net.URL
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.time.ZonedDateTime

import cats.MonadError
import cats.effect.SyncIO
import cats.syntax.all._
import io.circe.literal._
import io.circe.{Encoder, Json}
import org.scalatest.{FunSpecLike, Matchers}
import usafe.digital.didregistry.proof.ops._
import usafe.digital.didregistry.proof.types.SanitizedProof
import usafe.digital.didregistry.types.Did

class ProofSuite extends FunSpecLike with Matchers {

  val SigValueBase64: String = "loWIL7E56gOPoV1GpyuYe7ga/0cPxCtvk9iprD7h92fVxxdi1qZB6wyQM7v0tw3qLRKoWYnOAPGUNLOuvX0YkTX2owqzqm4lYz3TbhkAawO3nQwSOmKAcarDRpXhJeMSE6mIp9dWd9lyQC4wmAzonJN6C/5CyA7pjFt0tdjKbS+7GegTt34sFUK/DLor5x7xvT3m/YeVu8f2WqStwJcV08HN97ybJPFA6utBdFHHyMtiyzE/J4z+SqhASYoU4Adeuvil2XRKx4YofwraV84y/12JUN+gAmax8TVY63PBdlMENl+SjYufKpNWCz+dS7zTBWmgfM2jICWsFfY+zhMyVQ=="

  val Claim: Json =
    json"""
{
  "callbackReference": {
    "notifyURL": "http://localhost:3000/verifiable_credentials",
    "notificationFormat": "VerifiableCredential",
    "callbackData": {
      "VerifiableCredentialsRequestId": "fdc6bc33-00cd-41c8-89ec-4f5d97e4ef68"
    }
  },
  "credentialSubject": {
    "requestedParameters": ["name", "address", "phoneNumber"]
  },
  "proof": {
    "created": "2019-10-04T12:07:43.732Z",
    "signatureValue": $SigValueBase64,
    "creator": "did:usafe:2f2d1f47-de3d-4dfe-bab7-6fbff5bf8fe1#key1",
    "type": "RsaSignature2018"
  }
}
"""
  val PublicKeyBase64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA06ClIlHd+df+DE/rfJZuQWBqS4Os/MqEmbOE9MGkzlNqPGmjMbJ9W3gWaIjLTX0NLGUQ7r2CunfYFxJQ8omzzyys45ley5l0M8vkUh3FbUXq8Ix1X9y3mwH+l1a+demftBo+a2/0PrQ9Fzo90Hb/e/XZHmxCNgafMDd6M6STRqcsfyzgjGCOA0G8NMXe+7Ub+kwQaBCANyZug/gOxFP75AsGOSdrQ7m0hhVW7rj6xVsWUZXYtaWl3FZxaRnJWYtGsDYrkgVaiLC7A3CB0vI3EFLNKvMZaakE9ARhaTF8aqnmuz+J5X8dXqqUqiURblabhwsXDlbYjuykk1i9vp71NQIDAQAB"
  val pkBytes: Array[Byte] = PublicKeyBase64.base64Bytes[SyncIO].unsafeRunSync()
  val sigBytes: Array[Byte] = SigValueBase64.base64Bytes[SyncIO].unsafeRunSync()


  describe("proof verification") {
    it("checks proofValue is verifiable") {


      val canonicalClaim = ops.canonicalJson(Claim)
      val proof = canonicalClaim.hcursor.downField("proof").as[Json]
      val sanitizedClaim = SyncIO.fromEither(canonicalClaim.hcursor.downField("proof").delete.as[Json])
      val sanitizedProof =
        SyncIO.fromEither(proof.flatMap(_.hcursor.downField("type").delete.downField("signatureValue").delete.downField("creator").delete.as[Json]))

      val cry = Sha256WithRsaPss[SyncIO]

      val hashes = for {
        sc <- sanitizedClaim
        claimHash <- cry.hash(sc.noSpaces.utf8Bytes)
        sp <- sanitizedProof
        proofHash <- cry.hash(sp.noSpaces.utf8Bytes)
        _ <- SyncIO(info(s"Proof hash: ${ proofHash.toList.map("%02x".format(_)).mkString }"))
        _ <- SyncIO(info(s"Claim hash: ${ claimHash.toList.map("%02x".format(_)).mkString }"))
      } yield proofHash ++ claimHash

      val isValid = for {
        hs <- hashes
        r <- cry.verify(new X509EncodedKeySpec(pkBytes), hs, sigBytes)
      } yield r

      isValid.unsafeRunSync() shouldBe true
    }

    it("verifies its own signature") {
      import usafe.digital.didregistry.proof.implicits.{encodeProof, encodeSanitizedProof, decodeProof}

      implicit val enc: Encoder.AsObject[Json] = Encoder.encodeJsonObject.contramapObject { j =>
        j.asObject.get
      }


      val signedClaim = for {
        privK <- getKeyBytes[SyncIO]("test_rsa").map(new PKCS8EncodedKeySpec(_))
        sc = sanitizeJson(Claim)
        creator <- SyncIO.fromEither(Did.fromString("did:usafe:registry-1234567#key-1"))
        time = ZonedDateTime.now
        proof = SanitizedProof(creator, time)
        signed <- signDocument[SyncIO, Json](sc, proof, privK)
        _ = info(signed.spaces2)
      } yield signed

      val verified = for {
        pubK <- getKeyBytes[SyncIO]("test_rsa.pub").map(new X509EncodedKeySpec(_))
        d <- signedClaim
        v <- verifyProof[SyncIO, Json](d, pubK)
      } yield v

      verified.unsafeRunSync() shouldBe true

    }
  }


  private def loadResource[F[_] : MonadError[*[_], Throwable]](name: String): F[URL] = {
    val r = getClass.getClassLoader.getResource(name)
    if (r == null)
      MonadError[F, Throwable].raiseError(new RuntimeException(s"Not available: $name"))
    else
      MonadError[F, Throwable].pure(r)
  }

  private def bytesFromResource[F[_] : MonadError[*[_], Throwable]](url: URL): F[Array[Byte]] =
    MonadError[F, Throwable].catchNonFatal {
      val f = new File(url.toURI)
      val bytes = new Array[Byte](f.length().asInstanceOf[Int])
      new DataInputStream(new FileInputStream(f)).readFully(bytes)
      bytes
    }

  private def getKeyBytes[F[_]: MonadError[*[_], Throwable]](keyName: String): F[Array[Byte]] = for {
    u <- loadResource(keyName)
    bs <- bytesFromResource(u)
    str = new String(bs).replaceAll("-----((BEGIN)|(END))[A-Z\\s]+-----", "").replace("\n", "")
    base64Bs <- str.base64Bytes
  } yield base64Bs
}


