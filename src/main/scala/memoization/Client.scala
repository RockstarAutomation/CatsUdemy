package memoization

import cats.effect.IO
import cats.implicits.catsSyntaxParallelTraverse1

object IntroMemoization {
  case class Client(email: String)

  case class Email(body: String, recipients: List[String])

  trait EmailTemplates {
    def buildEmailForClient(templateId: String, client: Client): Email
  }

  def loadEmailTemplates(): IO[EmailTemplates] = ???

  def processClient(client: Client): IO[Unit] = ???

  def sendEmail(email: Email): IO[Unit] = ???

  def processClients(clients: List[Client]): IO[Unit] = {
    loadEmailTemplates().memoize.flatMap { templates =>
      clients.parTraverse { client =>
        processClient(client).handleErrorWith { _ =>
          templates.flatMap { emailTemp =>
            val email = emailTemp.buildEmailForClient("...", client)
            sendEmail(email)
          }
        }
      }
    }.void
  }
}
