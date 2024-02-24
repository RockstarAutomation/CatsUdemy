package errors

import cats.data.Validated.Invalid
import cats.data.{RWS, Validated, ValidatedNec}
import cats.effect.IO.{IOCont, Uncancelable}
import cats.effect.{ExitCode, IO, IOApp}
import errors.Controller.{Request, postTransfer}
import errors.Models.{Account, maxBalance}
import errors.Service.transfer
import errors.Validation.{validateAccNumber, validateDouble}

object Controller {
  case class Request(fromAcc: String, toAcc: String, amount: String)

  case class Response(status: Int, body: String)

  def postTransfer(request: Request): IO[Response] = {
    import Validation._

    if (validateAccNumber(request.fromAcc) && validateAccNumber(request.toAcc) && validateDouble(request.amount)) {
      import Service.transfer

      val res = transfer(request.fromAcc, request.toAcc, request.amount)
        .map {
          error =>
            error match
              case Left(error) => Response(400, error.toString)
              case Right(_) => Response(200, "Transaction completed")
        }
      res.pure[IO]
    } else {
      Response(400, "There is a problem with accounts or amount").pure[IO]
    }
  }

  def postTransfer2(request: Request): IO[Response] = {
    val res = (validateAccNumber(request.fromAcc), validateAccNumber(request.toAcc), validateDouble(request.amount)).tupled match {
      case Valid((fromAcc, toAcc, num)) => transfer(fromAcc, toAcc, num).map {
        case Left(_) => Response(200, "Transaction completed")
        case Right(error) => Response(400, error.toString)
      }
      case Invalid(error) => Response(400, error.toString).pure[IO]
    }

    res.handleErrorWith(_ => Response(500, "Server error")).pure[IO]
  }
}

object ErrorHandlingApp extends IOApp {

  override def run(args: List[String]) = {
    val request = Request("12345", "56789", "2000")
    postTransfer(request)
      .flatTap(bla => IO.println(bla))
      .as(ExitCode.Success)
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

case class InsufficientBalanceError(actualBalance: Double, amountToWithdraw: Double) extends DomainError

case class MaximumBalanceExceededError(maxDeposit: Double, balance: Double) extends DomainError

case class AccountNotFoundError(number: Double) extends DomainError

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

  def saveAccount(account: Account): IO[Unit] = data = data + (account.number -> account)
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