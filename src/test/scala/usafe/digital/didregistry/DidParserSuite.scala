package usafe.digital.didregistry

import cats.data.NonEmptyList
import org.scalatest.{FunSpecLike, Matchers}
import usafe.digital.didregistry.types._

class DidParserSuite extends FunSpecLike with Matchers {

  describe("DID parser") {
    it("parses a valid DID with a method name and a method specific ID") {
      val raw = "did:test:very-basic"
      val p = new DidParser(raw)
      p.InputLine.run().toEither shouldBe Right(Did(MethodName("test"), MethodSpecificId("very-basic")))
    }

    it("parses a valid DID with a method name, a method specific ID and method parameters") {
      val raw = "did:test:very-basic;a=b;very-basic:p=123"
      val p = new DidParser(raw)
      val result = p.InputLine.run().toEither
      result shouldBe Right(
        Did(
          MethodName("test"),
          MethodSpecificId("very-basic"),
          methodParameters = Option(NonEmptyList.fromListUnsafe(Parameter("a", "b") :: Parameter("very-basic:p", "123") :: Nil))
        )
      )
    }


    it("parses a valid DID with a method name, a method specific ID and a path") {
      val raw = "did:test:very-basic/path/to/salvation"
      val p = new DidParser(raw)
      val result = p.InputLine.run().toEither
      result shouldBe Right(
        Did(
          MethodName("test"),
          MethodSpecificId("very-basic"),
          path = Option(Path("/path/to/salvation"))
        )
      )
    }

    it("parses a valid DID with a method name, a method specific ID, method parameters and a path") {
      val raw = "did:test:very-basic;a=b;very-basic:p=123/path/to/salvation"
      val p = new DidParser(raw)
      val result = p.InputLine.run().toEither
      result shouldBe Right(
        Did(
          MethodName("test"),
          MethodSpecificId("very-basic"),
          methodParameters = Option(NonEmptyList.fromListUnsafe(Parameter("a", "b") :: Parameter("very-basic:p", "123") :: Nil)),
          path = Option(Path("/path/to/salvation"))
        )
      )
    }

    it("parses a valid DID with a method name, a method specific ID and query parameters") {
      val raw = "did:test:very-basic?with=query&with=repeat"
      val p = new DidParser(raw)
      p.InputLine.run().toEither shouldBe Right(
        Did(
          MethodName("test"),
          MethodSpecificId("very-basic"),
          queryParameters = Option(NonEmptyList.fromListUnsafe(Parameter("with", "query") :: Parameter("with", "repeat") :: Nil))
        )
      )
    }

    it("parses a valid DID with a method name, a method specific, method parameters, path and and query parameters") {
      val raw = "did:test:very-basic;a=b;very-basic:p=123/path/to/salvation?with=query&with=repeat"
      val p = new DidParser(raw)
      p.InputLine.run().toEither shouldBe Right(
        Did(
          MethodName("test"),
          MethodSpecificId("very-basic"),
          methodParameters = Option(NonEmptyList.fromListUnsafe(Parameter("a", "b") :: Parameter("very-basic:p", "123") :: Nil)),
          path = Option(Path("/path/to/salvation")),
          queryParameters = Option(NonEmptyList.fromListUnsafe(Parameter("with", "query") :: Parameter("with", "repeat") :: Nil))
        )
      )
    }

    it("parses a valid DID with a method name, a method specific, method parameters, a path, query parameters and a fragment") {
      val raw = "did:test:very-basic;a=b;very-basic:p=123/path/to/salvation?with=query&with=repeat#frag-me"
      val p = new DidParser(raw)
      p.InputLine.run().toEither shouldBe Right(
        Did(
          MethodName("test"),
          MethodSpecificId("very-basic"),
          methodParameters = Option(NonEmptyList.fromListUnsafe(Parameter("a", "b") :: Parameter("very-basic:p", "123") :: Nil)),
          path = Option(Path("/path/to/salvation")),
          queryParameters = Option(NonEmptyList.fromListUnsafe(Parameter("with", "query") :: Parameter("with", "repeat") :: Nil)),
          fragment = Option(Fragment("frag-me"))

        )
      )
    }

    it("parses a valid DID with a method name, a method specific ID and a fragment") {
      val raw = "did:test:very-basic#frag-me"
      val p = new DidParser(raw)
      p.InputLine.run().toEither shouldBe Right(
        Did(
          MethodName("test"),
          MethodSpecificId("very-basic"),
          fragment = Option(Fragment("frag-me"))
        )
      )
    }

  }

}
