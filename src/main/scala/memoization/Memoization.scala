package memoization

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.toFoldableOps

import scala.concurrent.duration.DurationInt

object Memoization extends IOApp {
  case class Client(name: String, email: String)

  case class Email(body: String, recipients: List[String])

  trait EmailTemplates {
    def buildEmailForClient(templateId: String, client: Client): Email
  }

  trait Error extends Throwable

  object NegativeBalance extends Error

  object AccountProblem extends Error

  // Long running computations
  def loadEmailTemplates(): IO[EmailTemplates] = IO.sleep(5.seconds) *>
    IO.println("Loding email templates") *>
    IO.pure((templateId: String, client: Client) => {
      if (templateId == "negative-balance") Email(s"Dear ${client.name}, your balance is negative", List(client.email))
      else Email(s"Dear ${client.name}, there is a problem with your acc", List(client.email))
    })

  def processClient(client: Client): IO[Unit] = {
    IO.println(s"Processing ${client.name}")
  }

  def sendEmail(email: Email): IO[Unit] = IO.println(s"Sending email to ${email.recipients}")

  private def processClients(clients: List[Client]): IO[Unit] = {
    loadEmailTemplates().memoize.flatMap { templatesIO =>
      clients.traverse_ { client =>
        processClient(client).handleErrorWith { error =>
          templatesIO.flatMap { templates =>
            error match {
              case NegativeBalance => sendEmail(templates.buildEmailForClient("negative-balance", client))
              case AccountProblem => sendEmail(templates.buildEmailForClient("account-problem", client))
              case e => IO.println(s"Unknown error: $e")
            }
          }
        }
      }
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val clients = List(Client("John", "john@mail.com"), Client("Alice", "alice@gmail.com"))
    processClients(clients)
      .as(ExitCode.Success)
  }
}
