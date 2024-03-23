package time

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

import scala.concurrent.duration.{FiniteDuration, SECONDS}

object Time extends IOApp {
  case class Token(value: String, expirationTimeInMillis: Long) {
    def isExpired(): IO[Boolean] = {
      IO.realTime.map(_.toMillis > expirationTimeInMillis)
    }
  }

  def measure[A](ioa: IO[A]): IO[FiniteDuration] = {
    for {
      start <- IO.monotonic
      _ <- ioa
      end <- IO.monotonic
    } yield (end - start)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    //    for {
    //      curTime <- IO.realTime
    //      token = Token("123", (curTime + FiniteDuration(10, SECONDS)).toMillis)
    //      isExpired <- token.isExpired()
    //      _ <- IO.println(s"Has token expired: $isExpired")
    //    } yield ExitCode.Success
    
    val program = (1 to 1000).toList.traverse_ { i =>
      IO.println(i)
    }
    measure(program)
      .map(_.toMillis)
      .flatTap(IO.println)
      .as(ExitCode.Success)
  }

}
