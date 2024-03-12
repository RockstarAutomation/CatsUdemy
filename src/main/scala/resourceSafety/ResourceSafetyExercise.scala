package resourceSafety

import cats.effect._
import cats.implicits._

import java.io._
import java.net.{HttpURLConnection, URL}

object ResourceSafetyExercise extends IOApp {
  def createConnection(targetURL: String): IO[HttpURLConnection] =
    IO.blocking {
      val connection = new URL(targetURL).openConnection().asInstanceOf[HttpURLConnection] // here
      connection.setRequestMethod("GET")
      connection
    }

  def readOutput(reader: BufferedReader): IO[String] =
    IO.blocking {
      Iterator
        .continually(reader.readLine)
        .takeWhile(_ != null)
        .mkString("\n")
    }

  def httpGet(targetURL: String): IO[String] = {
    //    for {
    //      connection <- createConnection(targetURL)
    //      reader <- IO(new BufferedReader(new InputStreamReader(connection.getInputStream))) // here
    //      resReader <- Resource.fromAutoCloseable(IO(reader)) // here
    //      response <- readOutput(resReader)
    //    } yield response

    createConnection(targetURL).bracket { connection =>
      val resRead = Resource.fromAutoCloseable(IO(new BufferedReader(new InputStreamReader(connection.getInputStream))))
      resRead.use(readOutput)
    } {
      connection =>
        IO(connection.disconnect())
    }
  }


  override def run(args: List[String]): IO[ExitCode] = {
    httpGet("http://www.google.com")
      .flatTap(IO.println)
      .as(ExitCode.Success)
  }
}
