package refs

import cats.effect.{Deferred, ExitCode, IO, IOApp}
import cats.implicits._

import scala.concurrent.duration.DurationInt

object DeferredApp extends IOApp {

  case class Item(id: Int)

  object ItemsLoadingError extends Error

  def loadItems(): IO[List[Item]] = {
    IO.raiseError(ItemsLoadingError)
//    IO.println("Loading items") *>
//      IO.sleep(2.seconds) *>
//      IO.println("Items loaded")
//        .as(List(Item(1), Item(1)))
  }

  def initUi(): IO[Unit] =
    IO.println("Initializing UI") *>
      IO.sleep(2.seconds) *>
      IO.println("UI initialized")

  def showItems(items: List[Item]): IO[Unit] = IO.println("Showing items")

  def showError(): IO[Unit] = IO.println("Showing Error")

  def setupUi(): IO[Unit] = {
    (initUi(), loadItems().attempt)
      .parMapN {
        case (_, Right(items)) => showItems(items)
        case (_, Left(_)) => showError()
      }
      .flatten
  }

  def handleUi(defItems: Deferred[IO, Either[Throwable, List[Item]]]): IO[Unit] =
    initUi() *> defItems.get.flatMap {
      case Right(items) => showItems(items)
      case Left(_) => showError()
    }

  def handleItems(defItems: Deferred[IO, Either[Throwable, List[Item]]]): IO[Unit] =
    loadItems().attempt.flatMap(defItems.complete).void

  def handleSetupUi(): IO[Unit] =
    Deferred[IO, Either[Throwable, List[Item]]].flatMap { deferItems =>
      List(handleUi(deferItems), handleItems(deferItems)).parSequence.void
    }

  override def run(args: List[String]): IO[ExitCode] = {
    handleSetupUi()
      .as(ExitCode.Success)
  }
}
