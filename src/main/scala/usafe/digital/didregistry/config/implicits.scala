package usafe.digital.didregistry.config

import cats.syntax.either._
import org.http4s.Uri
import pureconfig.error.CannotConvert
import pureconfig.{CamelCase, ConfigFieldMapping, ConfigReader}
import pureconfig.generic.ProductHint

object implicits {

  implicit def hint[T]: ProductHint[T] =
    ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase), allowUnknownKeys = true)

  implicit val uriReader: ConfigReader[Uri] = ConfigReader.fromString { s =>
    Uri.fromString(s).leftMap { err =>
      CannotConvert(s, "Uri", err.sanitized)
    }
  }

}
