package usafe.digital.didregistry

import cats.syntax.option._
import cats.data.NonEmptyList
import org.scalatest.{FunSpecLike, Matchers}
import org.http4s.implicits._
import usafe.digital.didregistry.types.{Did, DidDocument, DidDocumentValidationFault, PublicKey, PublicKeyEncoding, PublicKeyType, PublicKeyValue}

class OpsSuite extends FunSpecLike with Matchers {

  describe("DID document validation") {
    it("disallows insertion of a document with duplicate public key ID") {

      val dd = DidDocument(
        context = NonEmptyList.one(uri"http://localhost/context"),
        id = Did.fromStringUnsafe("did:localhost:test"),
        publicKey = NonEmptyList.fromListUnsafe(
          List(
            PublicKey(Did.fromStringUnsafe("did:localhost:test#unique"), PublicKeyType.RsaVerificationKey2018, Did.fromStringUnsafe("did:localhost:test"), PublicKeyValue("qodhqwohq", PublicKeyEncoding.PublicKeyPem)),
            PublicKey(Did.fromStringUnsafe("did:localhost:test#duplicate"), PublicKeyType.RsaVerificationKey2018, Did.fromStringUnsafe("did:localhost:test"), PublicKeyValue("qodhqwohq", PublicKeyEncoding.PublicKeyPem)),
            PublicKey(Did.fromStringUnsafe("did:localhost:test#duplicate"), PublicKeyType.RsaVerificationKey2018, Did.fromStringUnsafe("did:localhost:test"), PublicKeyValue("qodhqwohq", PublicKeyEncoding.PublicKeyPem))
          )
        ).some,
        authentication = none,
        controller = none,
        service = none
      )

      usafe.digital.didregistry.api.ops.validateDidDocument(dd) should matchPattern {
        case Left(DidDocumentValidationFault(_)) =>
      }

    }

    it("allows insertion of a document with NO duplicate public key ID") {

      val dd = DidDocument(
        context = NonEmptyList.one(uri"http://localhost/context"),
        id = Did.fromStringUnsafe("did:localhost:test"),
        publicKey = NonEmptyList.fromListUnsafe(
          List(
            PublicKey(Did.fromStringUnsafe("did:localhost:test#unique"), PublicKeyType.RsaVerificationKey2018, Did.fromStringUnsafe("did:localhost:test"), PublicKeyValue("qodhqwohq", PublicKeyEncoding.PublicKeyPem)),
            PublicKey(Did.fromStringUnsafe("did:localhost:test#also-unique"), PublicKeyType.RsaVerificationKey2018, Did.fromStringUnsafe("did:localhost:test"), PublicKeyValue("qodhqwohq", PublicKeyEncoding.PublicKeyPem)),
          )
        ).some,
        authentication = none,
        controller = none,
        service = none
      )

      usafe.digital.didregistry.api.ops.validateDidDocument(dd) should matchPattern {
        case Right(_) =>
      }

    }

  }

}
