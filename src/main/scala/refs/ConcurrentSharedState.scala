package refs

import cats.effect.{ExitCode, IO, IOApp, Ref}
import cats.implicits._

object ConcurrentSharedState extends IOApp {
  case class Activity()

  case class Purchase()

  case class Account()

  case class Customer(id: Long, name: String, account: List[Account], activities: List[Activity], purchase: List[Purchase])

  def loadName(id: Long)(ref: Ref[IO, List[String]]): IO[String] = {
    ref.update(logs => s"Loading name for customer id $id" :: logs) *>
      IO.pure(s"Cutomer $id")
  }

  def loadAccounts(id: Long)(ref: Ref[IO, List[String]]): IO[List[Account]] = ref.update(logs => s"Loading accounts for customer id $id" :: logs) *>
    IO.pure(List(Account()))

  def loadActivities(id: Long)(ref: Ref[IO, List[String]]): IO[List[Activity]] = ref.update(logs => s"Loading activities for customer id $id" :: logs) *>
    IO.pure(List(Activity()))

  def loadPurchases(id: Long)(ref: Ref[IO, List[String]]): IO[List[Purchase]] = ref.update(logs => s"Loading purchases for customer id $id" :: logs) *>
    IO.pure(List(Purchase()))

  def loadCustomer(id: Long)(ref: Ref[IO, List[String]]): IO[Customer] = (
    loadName(id)(ref),
    loadAccounts(id)(ref),
    loadPurchases(id)(ref),
    loadActivities(id)(ref)
    ).parMapN { case (name, accounts, purchases, activities) =>
    Customer(id, name, accounts, activities, purchases)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    (1 to 3)
      .map(_.toLong)
      .toList
      .parTraverse { id =>
        Ref.of[IO, List[String]](Nil).flatMap { ref =>
          loadCustomer(id)(ref)
            .flatTap(_ => ref.get.flatTap(logs => IO.println(logs.mkString("\n"))))
        }
      }
      .flatTap(IO.println)
      .as(ExitCode.Success)

    //    loadCustomer(5)
    //      .flatTap(IO.println)
    //      .as(ExitCode.Success)
  }
}
