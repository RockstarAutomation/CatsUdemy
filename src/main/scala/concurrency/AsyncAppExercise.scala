package concurrency

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.toFlatMapOps

object AsyncAppExercise extends IOApp {
  case class User(id: Long, username: String)

  type Error = String

  def findUserById(id: Long)(cb: Either[Error, User] => Unit): Unit = {
    if (math.random() < 0.5) cb(Right(User(id, s"Username$id")))
    else cb(Left("User not found"))
  }

  def findUserByIdIO(id: Long): IO[User] = {
    IO.async_ { cb =>
      findUserById(id) {
        case Right(user) => cb(Right(user))
        case Left(error) => cb(Left(new Exception(error)))
      }
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    findUserByIdIO(5)
      .flatTap(IO.println)
      .as(ExitCode.Success)
  }
}
