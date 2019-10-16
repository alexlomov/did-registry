package usafe.digital.didregistry.storage

import cats.effect.SyncIO
import io.circe.Decoder
import io.circe.literal._
import usafe.digital.didregistry.types.DidDocument

object data {

  import usafe.digital.didregistry.implicits._

  val DocJson = json"""
{
  "@context": [
    "https://www.w3.org/2019/did/v1",
    "https://w3id.org/security/v1"
  ],
  "id": "did:example:123456789abcdefghi",
  "publicKey": [
    {
      "id": "did:example:123456789abcdefghi#keys-1",
      "type": "RsaVerificationKey2018",
      "controller": "did:example:123456789abcdefghi",
      "publicKeyPem": "-----BEGIN PUBLIC KEY...END PUBLIC KEY-----"
    },
    {
      "id": "did:example:123456789abcdefghi#keys-2",
      "type": "Ed25519VerificationKey2018",
      "controller": "did:example:pqrstuvwxyz0987654321",
      "publicKeyBase58": "H3C2AVvLMv6gmMNam3uVAjZpfkcJCwDwnZn6z3wXmqPV"
    },
    {
      "id": "did:example:123456789abcdefghi#keys-3",
      "type": "Ed25519VerificationKey2018",
      "controller": "did:example:123456789abcdefghi",
      "publicKeyHex": "02b97c30de767f084ce3080168ee293053ba33b235d7116a3263d29f1450936b71"
    }
  ],
  "authentication": [
    "did:example:123456789abcdefghi#keys-1",
    "did:example:123456789abcdefghi#biometric-1",
    {
      "id": "did:example:123456789abcdefghi#keys-2",
      "type": "Ed25519VerificationKey2018",
      "controller": "did:example:123456789abcdefghi",
      "publicKeyBase58": "H3C2AVvLMv6gmMNam3uVAjZpfkcJCwDwnZn6z3wXmqPV"
    }
  ],
  "controller": "did:example:bcehfew7h32f32h7af3",
  "service": [
    {
      "id": "did:example:123456789abcdefghi#openid",
      "type": "OpenIdConnectVersion1.0Service",
      "serviceEndpoint": "https://openid.example.com/"
    },
    {
      "id": "did:example:123456789abcdefghi#vcr",
      "type": "CredentialRepositoryService",
      "serviceEndpoint": "https://repository.example.com/service/8377464"
    },
    {
      "id": "did:example:123456789abcdefghi#xdi",
      "type": "XdiService",
      "serviceEndpoint": "https://xdi.example.com/8377464"
    },
    {
      "id": "did:example:123456789abcdefghi#agent",
      "type": "AgentService",
      "serviceEndpoint": "https://agent.example.com/8377464"
    }
  ]
}
"""

  val DocJsonDuplicatePublicKeyId = json"""
{
  "@context": [
    "https://www.w3.org/2019/did/v1",
    "https://w3id.org/security/v1"
  ],
  "id": "did:example:123456789abcdefghi",
  "publicKey": [
    {
      "id": "did:example:123456789abcdefghi#keys-1",
      "type": "RsaVerificationKey2018",
      "controller": "did:example:123456789abcdefghi",
      "publicKeyPem": "-----BEGIN PUBLIC KEY...END PUBLIC KEY-----"
    },
    {
      "id": "did:example:123456789abcdefghi#keys-2",
      "type": "Ed25519VerificationKey2018",
      "controller": "did:example:pqrstuvwxyz0987654321",
      "publicKeyBase58": "H3C2AVvLMv6gmMNam3uVAjZpfkcJCwDwnZn6z3wXmqPV"
    },
    {
      "id": "did:example:123456789abcdefghi#keys-3",
      "type": "Ed25519VerificationKey2018",
      "controller": "did:example:123456789abcdefghi",
      "publicKeyHex": "02b97c30de767f084ce3080168ee293053ba33b235d7116a3263d29f1450936b71"
    },
    {
      "id": "did:example:123456789abcdefghi#keys-1",
      "type": "RsaVerificationKey2018",
      "controller": "did:example:123456789abcdefghi",
      "publicKeyPem": "-----BEGIN PUBLIC KEY...END PUBLIC KEY-----"
    }
  ],
  "authentication": [
    "did:example:123456789abcdefghi#keys-1",
    "did:example:123456789abcdefghi#biometric-1",
    {
      "id": "did:example:123456789abcdefghi#keys-2",
      "type": "Ed25519VerificationKey2018",
      "controller": "did:example:123456789abcdefghi",
      "publicKeyBase58": "H3C2AVvLMv6gmMNam3uVAjZpfkcJCwDwnZn6z3wXmqPV"
    }
  ],
  "controller": "did:example:bcehfew7h32f32h7af3",
  "service": [
    {
      "id": "did:example:123456789abcdefghi#openid",
      "type": "OpenIdConnectVersion1.0Service",
      "serviceEndpoint": "https://openid.example.com/"
    },
    {
      "id": "did:example:123456789abcdefghi#vcr",
      "type": "CredentialRepositoryService",
      "serviceEndpoint": "https://repository.example.com/service/8377464"
    },
    {
      "id": "did:example:123456789abcdefghi#xdi",
      "type": "XdiService",
      "serviceEndpoint": "https://xdi.example.com/8377464"
    },
    {
      "id": "did:example:123456789abcdefghi#agent",
      "type": "AgentService",
      "serviceEndpoint": "https://agent.example.com/8377464"
    }
  ]
}
"""

  val Doc: DidDocument =
    SyncIO.fromEither(Decoder[DidDocument].decodeJson(DocJson)).unsafeRunSync()

  val DocDuplicatePublicKeyId: DidDocument =
    SyncIO.fromEither(Decoder[DidDocument].decodeJson(DocJsonDuplicatePublicKeyId)).unsafeRunSync()

}
