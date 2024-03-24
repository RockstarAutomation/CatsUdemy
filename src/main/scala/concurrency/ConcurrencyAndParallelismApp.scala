package concurrency

import cats.effect._
import cats.implicits._
import cats.effect.implicits._
import scala.concurrent.duration._

object ConcurrencyAndParallelismApp extends IOApp {

  case class Image(data: List[Byte])

  object ImageService {
    def fetchFromDb(n: Int): IO[List[Image]] =
      IO.sleep(98.millis) *> (1 to n).toList.map(i => Image(List(i.toByte))).pure[IO]

    def fetchFromHttp(n: Int): IO[List[Image]] =
      IO.sleep(100.millis) *> (1 to n).toList.map(i => Image(List((100 + i).toByte))).pure[IO]
  }

  case class Person(name: String)

  object PersonService {
    def save(person: Person): IO[Long] = IO.sleep(100.millis) *> person.name.length.toLong.pure[IO]
  }

  case class Quote(author: String, text: String)

  object QuoteService {
    def fetchFromDb(n: Int): IO[List[Quote]] =
      IO.sleep(100.millis) *> (1 to n).toList.map(i => Quote(s"author $i", s"text $i")).pure[IO]

    def fetchFromHttp(n: Int): IO[List[Quote]] =
      IO.sleep(10.millis) *> (1 to n).toList.map(i => Quote(s"author $i", s"text $i")).pure[IO]

    def fetchAuthorAge(author: String): IO[Int] =
      IO.sleep(150.millis) *> IO((math.random() * 100).toInt)
  }

  override def run(args: List[String]): IO[ExitCode] = {

    // parMapN testing
    val n = 3

    //    (ImageService.fetchFromHttp(n), ImageService.fetchFromDb(n))
    //      .parMapN { case (httpsImages, dbImages) => httpsImages ++ dbImages }
    //      .flatTap(IO.println)
    //      .as(ExitCode.Success)

    // parTraverse testing
    //    val people = List(Person("Blla"), Person("Martin"), Person("Zupa"))
    //    people.parTraverse(PersonService.save).flatTap(IO.println).as(ExitCode.Success)

    // race testing
    //    IO.race(ImageService.fetchFromHttp(n), ImageService.fetchFromDb(n))
    //      .flatTap(IO.println)
    //      .as(ExitCode.Success)

    // exercise

    IO.race(QuoteService.fetchFromHttp(n), QuoteService.fetchFromDb(n))
      .flatMap {
        _.fold(identity, identity).parTraverse(q => QuoteService.fetchAuthorAge(q.author))
      }
      .map(ages => ages.sum / n)
      .flatTap(IO.println)
      .as(ExitCode.Success)

  }

}
