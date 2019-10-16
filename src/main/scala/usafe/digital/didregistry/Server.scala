package usafe.digital.didregistry

import cats.effect.{ContextShift, ExitCode, IO, IOApp, Async}
import cats.implicits._
import pureconfig.error.ConfigReaderException
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import pureconfig._
import org.http4s.syntax.kleisli._
import pureconfig.generic.auto._
import usafe.digital.didregistry.api.Endpoint
import usafe.digital.didregistry.config.types._
import usafe.digital.didregistry.config.implicits._
import usafe.digital.didregistry.storage.jdbc.StorageOps


object Server extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = for {
    settings <- IO.fromEither(
      ConfigSource.default.load[AppConfig].leftMap(ConfigReaderException.apply)
    )
    http <- httpRoutes[IO](settings.jdbcConnection)
    code <- BlazeServerBuilder[IO]
      .bindHttp(settings.httpServer.port.value, settings.httpServer.iface.ip)
        .withHttpApp(http.orNotFound)
        .serve.compile.lastOrError

  } yield code


  private def httpRoutes[F[_]: Async: ContextShift](jdbcConnection: JdbcConnection): F[HttpRoutes[F]] = for {
    store <- Async[F].pure(StorageOps[F](jdbcConnection))
    endpoint = Endpoint[F]
    r = Router[F]("/" -> (endpoint.getDidDocument(store.load) <+> endpoint.postDidDocument(store.store)))
  } yield r

}
