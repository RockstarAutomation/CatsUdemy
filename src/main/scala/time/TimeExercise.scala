package time

import cats.effect._
import cats.implicits._

import java.time.{Instant, LocalDateTime, ZoneId, ZoneOffset}
import scala.concurrent.duration.{DurationInt, FiniteDuration}

object TimeExercise extends IOApp {
  def tomorrow(): IO[FiniteDuration] = IO.realTime.map(_ + 1.day)

  def tomorrowDateTime(): IO[LocalDateTime] =
    tomorrow()
      .map(time => LocalDateTime.ofInstant(Instant.ofEpochMilli(time.toMillis), ZoneId.systemDefault()))
      .flatTap(IO.println)

  override def run(args: List[String]): IO[ExitCode] = {
    tomorrowDateTime()
      .flatTap(IO.println)
      .as(ExitCode.Success)
  }

}
