package refs

import cats.effect.{Deferred, ExitCode, IO, IOApp}

object DeferredAppExercise extends IOApp {
  class Producer[A](name: String, deferred: Deferred[IO, A], exec: IO[A]) {
    def run(): IO[Unit] = {
      IO.println("Producer produces...") *>
        exec.flatMap(a => deferred.complete(a)).void *>
        IO.println("Producer produced!")
    }
  }

  class Consumer[A](name: String, deferred: Deferred[IO, A], consume: A => IO[Unit]) {
    def run(): IO[Unit] = {
      IO.println("Consumer consumes...") *>
        deferred.get.flatMap { df =>
          consume(df)
        } *>
        IO.println("Consumer consumed!")
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    Deferred[IO, Int].flatMap { deferred =>
      val producer = new Producer[Int]("Producer", deferred, IO.pure(42))
      val consumer = new Consumer[Int]("Consumer", deferred, i => IO.println(i))
      consumer.run().both(producer.run())
    }.as(ExitCode.Success)
  }

}
