package queue

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import cats.effect.std.Queue

import javax.imageio.ImageIO
import java.io.File
import java.awt.image.BufferedImage
import java.awt.color.ColorSpace
import java.awt.image.ColorConvertOp
import scala.concurrent.duration.DurationInt

object QueueExercise extends IOApp {
  case class ImageInfo(filepath: String, image: BufferedImage)

  def processImage(imageInfo: ImageInfo): ImageInfo = {
    val colorOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null)
    val processedImage = colorOp.filter(imageInfo.image, imageInfo.image)
    IO.println("Processing image")
    imageInfo.copy(image = processedImage)
  }

  def saveImage(image: ImageInfo): IO[Unit] = {
    IO.blocking {
      val fp = image.filepath
      val newPath = s"${fp.substring(0, fp.length - 4)}_processed.jpg"
      IO.println(s"writing to $newPath")
      ImageIO.write(image.image, "jpg", new File(s"$newPath"))
    }.void
  }

  def loadImages(directory: String): IO[List[ImageInfo]] = {
    for {
      dir <- IO.blocking(new File(directory))
      files <- IO.blocking(dir.listFiles.toList.filter(f => f.isFile && f.getName.endsWith("jpg")))
      images <- files.parTraverse(f => IO.blocking(ImageInfo(f.getAbsolutePath, ImageIO.read(f))))
    } yield images
  }

  def imageSaver(processedImageQueue: Queue[IO, ImageInfo]): IO[Unit] = {
    processedImageQueue.take.flatMap(saveImage).foreverM
  }

  def imageProcessor(rawImageQueue: Queue[IO, ImageInfo], processedImageQueue: Queue[IO, ImageInfo]): IO[Unit] = {
    rawImageQueue.take.flatMap { rawImage =>
      processedImageQueue.offer(processImage(rawImage))
    }.foreverM
  }

  def imageLoader(srcDir: String, rawImageQueue: Queue[IO, ImageInfo]): IO[Unit] = {
    loadImages(srcDir)
      .flatMap { images =>
        IO.println(s"Loading image: $images") *>
          images.parTraverse_(rawImageQueue.offer)
      }
  }

  def start(srcDirs: List[String], noProcessors: Int, noSavers: Int): IO[Unit] = {

    Queue.unbounded[IO, ImageInfo].flatMap { queueRaw =>
      Queue.unbounded[IO, ImageInfo].flatMap { queueProcessed =>
        val imgLoaders = srcDirs.map { dir =>
          IO.println(s"Currently loading images from: $dir") *>
            imageLoader(dir, queueRaw)
        }
        val imageProcessors = List.range(0, noProcessors).map(_ => imageProcessor(queueRaw, queueProcessed))
        val imageSavers = List.range(0, noSavers).map(_ => imageSaver(queueProcessed))
        (imgLoaders ++ imageProcessors ++ imageSavers).parSequence_
      }
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val dirs = List("kittens", "puppies")
    start(dirs, 16, 16).timeoutTo(30.seconds, IO.unit).as(ExitCode.Success)

  }

}
