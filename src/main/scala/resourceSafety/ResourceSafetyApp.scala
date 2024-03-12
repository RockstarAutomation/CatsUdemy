package resourceSafety

import cats.effect._
import cats._
import cats.implicits._
import cats.effect.implicits._

import java.io.{File, FileInputStream, FileOutputStream}

object ResourceSafetyApp extends IOApp {

  def write(bytes: Array[Byte], fos: FileOutputStream): IO[Unit] =
    IO(println(s"Writing ${bytes.length} bytes")) *> IO.blocking(fos.write(bytes))

  def read(fis: FileInputStream): IO[Array[Byte]] =
    IO.println("Reading") *> IO.blocking {
      Iterator
        .continually(fis.read)
        .takeWhile(_ != -1)
        .map(_.toByte)
        .toArray
    }

  def encrypt(bytes: Array[Byte]): IO[Array[Byte]] =
    IO.println("encrypting") *> bytes.map(b => (b + 1).toByte).pure[IO]

  def closeWriter(ac: AutoCloseable): IO[Unit] =
    IO.println("closing writer") *> IO.blocking(ac.close())

  def closeReader(ac: AutoCloseable): IO[Unit] = {
    IO.println("closing reader") *> IO.blocking(ac.close())
  }

  private def encryptFile(srcFile: File, destFile: File): IO[Unit] = {
    val acquireReader = IO.println("acquiring reader") *> IO.blocking(new FileInputStream(srcFile))
    val acquireWriter = IO.println("acquiring writer") *> IO.blocking(new FileOutputStream(destFile))

    acquireReader.bracket { reader =>
      acquireWriter.bracket { writer =>
        IO.println("processing") *>
          read(reader).flatMap(encrypt).flatMap(write(_, writer))
      }(closeWriter)
    }(closeReader)
  }

  private def encryptFileWithResource(srcFile: File, destFile: File): IO[Unit] = {
    val reader = Resource.make(IO.println("acquiring reader") *> IO.blocking(new FileInputStream(srcFile)))(closeReader)
    val writer = Resource.make(IO.println("acquiring writer") *> IO.blocking(new FileOutputStream(destFile)))(closeWriter)
    val readerAndWriterRes = reader.flatMap(r => writer.map(w => (r, w)))

    readerAndWriterRes.use { case (reader, writer) =>
      //      IO.raiseError(new Exception("boom")) *> // testing error handling
      IO.println("processing") *>
        read(reader).flatMap(encrypt).flatMap(write(_, writer))
    }
  }

  private def encryptFileWithAutoCloseable(srcFile: File, destFile: File): IO[Unit] = {
    val reader = Resource.fromAutoCloseable(IO.println("acquiring reader") *> IO.blocking(new FileInputStream(srcFile)))
    val writer = Resource.fromAutoCloseable(IO.println("acquiring writer") *> IO.blocking(new FileOutputStream(destFile)))
    val readerAndWriterRes = reader.flatMap(r => writer.map(w => (r, w)))

    readerAndWriterRes.use { case (reader, writer) =>
      //      IO.raiseError(new Exception("boom")) *> // testing error handling
      IO.println("processing") *>
        read(reader).flatMap(encrypt).flatMap(write(_, writer))
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val srcFile = new File("test.txt")
    val destFile = new File("test_encrypted.txt")
    encryptFileWithResource(srcFile, destFile).as(ExitCode.Success)
  }
}
