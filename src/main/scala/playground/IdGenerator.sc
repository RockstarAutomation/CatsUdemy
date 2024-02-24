import cats.data.State

class IdGenerator {
  var counter: Int = 0

  def next(): Int = {
    counter += 1
    counter
  }
}

class AccountError(msg: String) extends Exception(msg)

case class InsufficientBalanceError(toWithdraw: Double, balance: Double) extends AccountError(s"Withdraw $toWithdraw. Insufficient balance: $balance")


case class Person(id: Int, name: String, group: Int)

def createPerson(name: String, idGenerator: IdGenerator): Person = {
  val group = idGenerator.next() % 2
  Person(idGenerator.next(), name, group)
}

type Db = Map[Int, Person]

class InMemoryPersonDB {

  def add(person: Person): State[Db, Unit] = {
    State.modify(_ + (person.id -> person))
  }

  def delete(id: Int): State[Db, Unit] = {
    State.modify(_ - id)
  }

  def find(id: Int): State[Db, Option[Person]] = {
    State.inspect(_.get(id))
  }
}

case class Account(id: Int, balance: Double, owner: Person) {
  def withdraw(amount: Double): Either[AccountError, Account] =
    if (amount > balance) Left(InsufficientBalanceError(amount, balance))
    else Right(copy(balance = balance - amount))
}

// idGen test
val idGen = new IdGenerator
createPerson("Leannardo", idGen) == Person(2, "Leannardo", 1)
createPerson("Martin", idGen) == Person(4, "Martin", 1)
createPerson("Eugenia", idGen) == Person(6, "Eugenia", 1)

// inMemory test
val program: Option[Person] = {
  val db = new InMemoryPersonDB
  for {
    _ <- db.add(Person(2, "Leannardo", 1))
    _ <- db.delete(2)
    _ <- db.add(Person(2, "Leannardo", 1))
    p <- db.find(2)
  } yield p
}.runA(Map.empty).value

// Account test
val program2: Double = {
  val person = Person(2, "Leannardo", 1)
  val account = Account(1, 1000, Person(2, "Leannardo", 1))
  val withdrawFc = account.withdraw(2000)
  if(person.name == "Leannardo") {
    5000
  } else withdrawFc.fold(_ => 0, _.balance)
}
