package concurrency


import cats._
import cats.effect._
import cats.implicits._
import cats.effect.implicits._

import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object AsyncApp extends IOApp {
  case class Person(id: String, name: String)

  def findPersonById(id: String)(implicit ec: ExecutionContext): Future[Person] = Future {
    println(s"Thread: ${Thread.currentThread().getName}")
    Person(id, "Bla")
  }

  def findPersonByIdIO(id: String): IO[Person] = {
    // By autopilot
    //  IO.fromFuture(IO(findPersonById(id)(ExecutionContext.global)))
    implicit val ec: ExecutionContextExecutor = ExecutionContext.global
    IO(Await.result(findPersonById(id), Duration.Inf))
  }

  def findPersonByIdIO2(id: String): IO[Person] = {
    IO.executionContext.flatMap { implicit ec =>
      IO.async_(cb => findPersonById(id).onComplete {
        case Success(value) => cb(Right(value))
        case Failure(exception) => cb(Left(exception))
      })
    }
  }

  def findPersonByIdIO3(id: String): IO[Person] = {
    IO.executionContext.flatMap { implicit ec =>
      IO.fromFuture(IO(findPersonById(id)))
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    // Should be closed after execution
    implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
    findPersonByIdIO3("123")
      .evalOn(ec)
      .flatTap(IO.println)
      .as(ExitCode.Success)
  }
}
