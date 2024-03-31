package threads

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import scala.concurrent.duration.DurationInt

object ComputeBlockingApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    def task(id: Long): IO[Unit] =
      IO.blocking(println(s"Running task $id on the thread ${Thread.currentThread().getName}"))

    def blockingTask(id: Long): IO[Unit] = IO.blocking {
      println(s"Running task $id on the thread ${Thread.currentThread().getName}")
      Thread.sleep(2000)
      println(s"Waking up $id on the thread ${Thread.currentThread().getName}")
    }

    (1 to 1000).toList.parTraverse { i =>
      task(i)
    }
      .timeoutTo(5.seconds, IO.unit)
      .as(ExitCode.Success)
  }

}
