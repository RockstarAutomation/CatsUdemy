package errors

import cats.data.Validated.{Invalid, Valid}
import cats.data.{Validated, ValidatedNec}
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId, catsSyntaxFoldableOps0, catsSyntaxTuple3Semigroupal, toFlatMapOps, toTraverseOps}
import errors.Controller.{Request, postTransfer}
import errors.Models.Account

import java.io.{FileInputStream, FileNotFoundException}
import scala.io.Source
import scala.util.control.NonFatal

object Controller {
  case class Request(fromAccount: String, toAccount: String, amount: String)

  case class Response(status: Int, body: String)

  import Validation._

  // Validate the from account number, the to account number and the amount
  // If validations fail, return a Response with code 400 and some error message
  // Otherwise, call transfer
  // If there is any domain error, return a Response with code 400 and some error message
  // If there is any other error, return a Response with code 500 and Internal Server Error message
  // Otherwise, return a Response with code 200 and Transfer successfully executed
  def postTransfer(request: Request): IO[Response] = {
    val response = (validateAccNumber(request.fromAccount),
      validateAccNumber(request.toAccount),
      validateDouble(request.amount)).tupled match {
      case Valid((fromAccountNumber, toAccountNumber, amount)) =>
        Service.transfer(fromAccountNumber, toAccountNumber, amount).map {
          case Right(()) =>
            Response(200, "Transfer successfully executed")

          case Left(error) =>
            Response(400, error.toString)
        }

      case Invalid(errors) =>
        Response(400, errors.mkString_(", ")).pure[IO]
    }
    response.handleErrorWith {
      case NonFatal(e) => Response(500, "Internal server error").pure[IO]
    }
  }
}

object ErrorHandlingApp extends IOApp {

  override def run(args: List[String]) = {
    //    val request = Request("12345", "56789", "2000")
    //    postTransfer(request)
    //      .flatTap(bla => IO.println(bla))
    //      .as(ExitCode.Success)

    val bla = IO.blocking(new FileInputStream(args.head))
      .bracket {
        fis =>
          IO.blocking(
            Iterator
              .continually(fis.read)
              .takeWhile(_ != -1)
              .map(_.toByte)
              .toArray
          )
      } {
        fis => IO.blocking(fis.close())
      }
    bla.as(ExitCode.Success)
  }
  def loadFile(filename: String): IO[Either[DomainError, String]] = {
    def loadFileContents(filename: String): IO[Array[Byte]] = {
      IO.blocking(new FileInputStream(filename))
        .bracket { fis =>
          IO.blocking(
            Iterator
              .continually(fis.read)
              .takeWhile(_ != -1)
              .map(_.toByte)
              .toArray
          )
        } { fis =>
          IO.blocking(fis.close())
        }
    }

    /* 1 */
    // Implement a load file function that loads all the contents of a file into a String
    // If the file does not exist, capture that with the domain error TextFileNotFound
          loadFileContents(filename).map { bytes =>
            new String(bytes).asRight[DomainError]
          }.handleErrorWith {
            case _: FileNotFoundException => TextFileNotFound(filename).asLeft[String].pure[IO]
            case t: Throwable => IO.raiseError(t)
          }
    IO.raiseError(new StackOverflowError("boom"))
  }
}

object Validation {
  type Valid[A] = ValidatedNec[String, A]

  def validateDouble(s: String): Valid[Double] = Validated.fromOption(s.toDoubleOption, s"$s is not a valid double").toValidatedNec

  def validateAccNumber(accNumber: String): Valid[String] = {
    Validated.condNec(accNumber.forall(_.isLetterOrDigit),
      accNumber,
      s"The acc number $accNumber must be bla bla bla")
  }
}

trait DomainError

case class TextFileNotFound(filename: String) extends DomainError


case class InsufficientBalanceError(actualBalance: Double, amountToWithdraw: Double) extends DomainError

case class MaximumBalanceExceededError(maxDeposit: Double, balance: Double) extends DomainError

case class AccountNotFoundError(number: String) extends DomainError

object Models {
  val maxBalance = 5000

  case class Account(number: String, balance: Double) {
    def withdraw(amount: Double): Either[DomainError, Account] = {
      if (amount > balance) {
        Left(InsufficientBalanceError(balance, amount))
      } else {
        Right(this.copy(balance = balance - amount))
      }
    }

    def deposit(amount: Double): Either[DomainError, Account] = {
      if ((amount + balance) > maxBalance) {
        Left(MaximumBalanceExceededError(maxBalance - amount, balance))
      } else {
        Right(this.copy(balance = balance + amount))
      }
    }
  }
}

object Repository {

  import Models.Account

  var data = Map.empty[String, Account]

  def findAccountByNumber(number: String): IO[Option[Account]] = data.get(number).pure[IO]

  def saveAccount(account: Account): IO[Unit] = (data = data + (account.number -> account)).pure[IO]
}

object Service {

  def transfer(fromAccNumber: String, toAccNumber: String, amount: Double): IO[Either[DomainError, Unit]] = {
    import Repository._

    findAccountByNumber(fromAccNumber).flatMap {
      fromAccountOpt =>
        findAccountByNumber(toAccNumber).flatMap {
          toAccountOpt =>
            val accounts: Either[DomainError, (Account, Account)] = for {
              from <- fromAccountOpt.toRight(AccountNotFoundError(fromAccNumber))
              to <- toAccountOpt.toRight(AccountNotFoundError(toAccNumber))
              updatedFrom <- from.withdraw(amount)
              updatedTo <- to.deposit(amount)
            } yield (updatedFrom, updatedTo)

            accounts.traverse { case (fromAcc, toAcc) =>
              saveAccount(fromAcc) *> saveAccount(toAcc)
            }
        }
    }
  }
}