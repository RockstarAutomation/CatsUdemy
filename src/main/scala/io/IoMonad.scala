package io

import cats.effect.IO.{IOCont, Uncancelable}
import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.unsafe.implicits.global

import scala.io.StdIn

object IoMonad extends IOApp {
  object Console {
    def putStrLn(s: String): IO[Unit] = IO(println(s))

    def readLine(text: String): IO[String] = IO(StdIn.readLine(text))
  }

  def echoForever: IO[Nothing] = {
    import Console._
//    IO.delay()
    readLine("enter ur name:").flatMap(str => putStrLn(str)).foreverM
  }

  override def run(args: List[String]): IO[ExitCode] = {
    //    import Console._
    //    for {
    //      s <- readLine("enter ur name:")
    //      t <- readLine("enter smth:")
    //      _ <- putStrLn(s ++ t)
    //    } yield ExitCode.Success
    echoForever
  }

  //  def main(args: Array[String]): Unit = {
  //    import Console._
  //    readLine("Enter ur name: ")
  //      .flatMap(name => putStrLn(s"Hey $name"))
  //      .unsafeRunSync()
  //
  //    putStrLn("Hello") *> putStrLn("world") unsafeRunSync() // *> == >>
  //
  //    putStrLn("Thinking...").foreverM.unsafeRunSync() // runs forever
  //  }

}
