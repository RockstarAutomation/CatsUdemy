package typeclasses

import cats.Parallel
import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits._

object TaglessFinalExercise extends IOApp {

  case class Image(data: List[Byte])

  trait ImageService[F[_]] {
    def fetchFromDb(n: Int): F[List[Image]]

    def fetchFromHttp(n: Int): F[List[Image]]

    def fetchBoth(n: Int): F[List[Image]]
  }

  object ImageService {
    def impl[F[_]: Sync: Parallel]: ImageService[F] = new ImageService[F] {
      override def fetchFromHttp(n: Int): F[List[Image]] = {
        List.range(0, n).parTraverse { i =>
          Sync[F].blocking(Image(List(i.toByte)))
        }
      }

      override def fetchFromDb(n: Int): F[List[Image]] = {
        List.range(0, n).parTraverse { i =>
          Sync[F].blocking(Image(List((100 + i).toByte)))
        }
      }

      override def fetchBoth(n: Int): F[List[Image]] = {
        (fetchFromHttp(n), fetchFromDb(n)).parMapN { case (httpImages, dbImages) => httpImages ++ dbImages }
      }
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val imageService = ImageService.impl[IO]
    imageService.fetchBoth(10)
      .flatTap(IO.println)
      .as(ExitCode.Success)
  }

}
