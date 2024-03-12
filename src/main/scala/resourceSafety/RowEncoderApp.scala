package resourceSafety

import cats.effect.kernel.Resource
import cats.effect.{ExitCode, IO, IOApp}

import java.io.{BufferedWriter, File, FileWriter}
import scala.util.Using

object RowEncoderApp extends IOApp {

  trait RowEncoder[A] {
    def encode(value: A): String
  }

  case class Person(name: String, age: Int)

  implicit val personEncoder: RowEncoder[Person] = (value: Person) => s"${value.name},${value.age}"

  private def writeAll[A](values: List[A], file: File)(implicit encoder: RowEncoder[A]): IO[Unit] = {
    // alternatively to Using, the bracket()() func could be used to execute write and close
    IO.blocking(Using(new BufferedWriter(new FileWriter(file))) { writer =>
      values.foreach { value =>
        val line = encoder.encode(value)
        writer.write(line + "\n")
      }
    })
  }

  private def writeAllWithBrackets[A](values: List[A], file: File)(implicit encoder: RowEncoder[A]): IO[Unit] = {
    val contents = values.map(encoder.encode).mkString("\n")

    // Define the 'use' function to write the contents to the FileWriter
    def use(fw: FileWriter): IO[Unit] = IO.blocking {
      fw.write(contents) // Write the contents to the FileWriter
      fw.flush() // Flush the FileWriter to ensure data is written to the file
    }

    // Define the 'release' function to close the FileWriter
    def release(fw: FileWriter): IO[Unit] = IO.blocking {
      fw.close() // Close the FileWriter
    }

    // Use 'bracket' to ensure proper resource management (open, use, close)
    IO.blocking(new FileWriter(file)).bracket(use)(release)
    // to avoid calling the release function, use Resource.fromAutoCloseable
    // Resource.fromAutoCloseable(IO(new FileWriter(file))).use(use)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val file = new File("test2.txt")
    val persons = List(Person("Alice", 25), Person("Bob", 30))
    writeAllWithBrackets(persons, file).as(ExitCode.Success)
  }
}
