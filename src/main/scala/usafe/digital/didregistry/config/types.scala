package usafe.digital.didregistry.config

import cats.syntax.option._
import org.http4s.Uri

object types {

  final case class AppConfig(
    httpServer: HttpServer,
    jdbcConnection: JdbcConnection
  )

  final case class HttpServer(iface: Iface, port: Port)

  final case class Iface(ip: String) extends AnyVal

  final case class Port(value: Int) extends AnyVal

  final case class JdbcConnection(
    driverInfo: DriverInfo,
    connectionString: Uri,
    user: Option[String] = none,
    password: Option[String] = none
  )

  final case class DriverInfo(value: String) extends AnyVal

}
