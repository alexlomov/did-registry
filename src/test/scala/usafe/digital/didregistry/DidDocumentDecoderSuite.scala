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
          |{
          |  "@context": "https://www.w3.org/2019/did/v1",
          |  "id": "did:usafe:ec89402c-9d1c-497b-810f-2ae935e7fe38",
          |  "publicKey": [
          |    {
          |      "id": "did:usafe:78110be7-16ac-4796-ab42-2a3d3d6874e6#key1",
          |      "type": "RsaVerificationKey2018",
          |      "controller": "did:usafe:78110be7-16ac-4796-ab42-2a3d3d6874e6",
          |      "publicKeyPem": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6DZQDW8y87jQYWpDUrcvSbZdamerH5dYeH2uAFyQr3/60sNyZF24oAe0TWzb7GTqEdQOy2ocw/jyRhuSNiqd3ZHya1tx8vlasUCw1tcWhZrlK2VohvMtLtaGmK9MFTCNG4adNYUwk7DQD1OhrZXEa6MARZAxeTkkHcn2pGslM/GzDelhlc8cPAbEGrbwugJVoM7Kq5dTwBq+8go74yVfODW+gcm/Xz3kSDb3uoGiXuRaTb59wbGPqbjZxjDrm6sOXbjK0pnzYJwptoPFEXx/vKgx8U1XmWOqUDDSgnk2a+yvmcj3f3uGHbBAieI1SmWM5aUlw0ejurpLqaUbtqLOpwIDAQAB"
          |    }
          |  ],
          |  "authentication": [
          |    {
          |      "type": "RsaVerificationKey2018",
          |      "publicKey": "did:usafe:78110be7-16ac-4796-ab42-2a3d3d6874e6#key1"
          |    }
          |  ],
          |  "created": "2019-11-12T17:28:41.910Z"
          |}
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
