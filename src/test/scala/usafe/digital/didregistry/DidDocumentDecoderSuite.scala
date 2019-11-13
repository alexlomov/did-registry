package usafe.digital.didregistry

import org.scalatest.{FunSpecLike, Matchers}
import io.circe.parser._
import usafe.digital.didregistry.types.DidDocument
import usafe.digital.didregistry.implicits.didDocumentDecoder

class DidDocumentDecoderSuite extends FunSpecLike with Matchers {
  describe("DID document JSON decoder") {

    it("decodes a valid DID document") {
      val parsedDidDoc: Either[Throwable, DidDocument] = decode(storage.data.DocJson.noSpaces)
      parsedDidDoc.isRight shouldBe true

    }

    it("decodes a document generated from the OpenAPI specification.") {
      val jsonStr =
        """
          {
            "@context": "https://www.w3.org/2019/did/v1",
            "id": "did:usafe:4648c76a-51e8-46c8-a10d-02147d423935",
            "publicKey": [
              {
                "id": "did:usafe:b9607c82-498c-4667-be8a-148b5d0e7b5d#key1",
                "type": "RsaVerificationKey2018",
                "controller": "did:usafe:b9607c82-498c-4667-be8a-148b5d0e7b5d",
                "publicKeyPem": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2lwoqxBK5k/D/zB2neUhYAOLVaFOUSa6robiGZdu/KQodUltJ9ef9TOcc/ktRnuGg2ir94Hj6eW3yEVq3ZAqDn1jwsHz0TB7DMe0Slv0Lw0POeFldSZAi6eOku+iSFyKkrMuu3jxcGYB7H8HSr1Vyf/SKlYoYjifk262MgY2P1/kuoMC+JE+OrkE+Hv4iWV7LzvWYt8nFOyTIPIF/F+IU2y2vxxEeH0jFH84oBrR65zbUaM9U9kN8SZSi3gesNmUmJ3hGAjLbaZ0upUMdaTvcDxDAMremkCGPUNRFcbNJD04jzgYbFGN7xg6CVkZPQbQFdKAhH1gM7vyUzyxhEW7WQIDAQAB"
              }
            ],
            "authentication": [
              {
                "id": "did:usafe:b9607c82-498c-4667-be8a-148b5d0e7b5d#key1",
                "type": "RsaVerificationKey2018",
                "controller": "did:usafe:b9607c82-498c-4667-be8a-148b5d0e7b5d",
                "publicKeyPem": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2lwoqxBK5k/D/zB2neUhYAOLVaFOUSa6robiGZdu/KQodUltJ9ef9TOcc/ktRnuGg2ir94Hj6eW3yEVq3ZAqDn1jwsHz0TB7DMe0Slv0Lw0POeFldSZAi6eOku+iSFyKkrMuu3jxcGYB7H8HSr1Vyf/SKlYoYjifk262MgY2P1/kuoMC+JE+OrkE+Hv4iWV7LzvWYt8nFOyTIPIF/F+IU2y2vxxEeH0jFH84oBrR65zbUaM9U9kN8SZSi3gesNmUmJ3hGAjLbaZ0upUMdaTvcDxDAMremkCGPUNRFcbNJD04jzgYbFGN7xg6CVkZPQbQFdKAhH1gM7vyUzyxhEW7WQIDAQAB"
              }
            ],
            "created": "2019-11-13T15:52:29.762Z"
          }
          |""".stripMargin

      val decoded: Either[Throwable, DidDocument] = decode(jsonStr)
      info(
        decoded.fold(
          _.getMessage,
          _.toString
        )
      )
      decoded.isRight shouldBe true
    }
  }

}
