package refs

import cats.effect.{ExitCode, IO, IOApp, Ref}
import cats.implicits._

import scala.annotation.tailrec

object ConcurrentSharedStateExercise extends IOApp {
  case class User(name: String, age: Int, friends: List[User])

  def findOldest(user: User): IO[User] = {
    Ref.of[IO, User](user).flatMap{ref =>
      findOldest2(user, ref) *> ref.get
    }
  }

  def findOldest2(user: User, ref: Ref[IO, User]): IO[Unit] = {
    val handleRoot = ref.update { ref =>
      if (user.age > ref.age) user
      else ref
    }

    val handleFriends = user.friends.parTraverse_(friend => findOldest2(friend, ref))
    handleRoot.both(handleFriends).void
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val user1 = User("Alice", 125, Nil)
    val user2 = User("Bob", 70, Nil)
    val user3 = User("Charlie", 35, Nil)
    val user4 = User("David", 40, List(user2, user3))
    val user5 = User("Eve", 29, List(user1, user4))
    val user6 = User("Frank", 30, List(user5))

    findOldest(user6)
      .flatTap(IO.println)
      .as(ExitCode.Success)
  }

}

// user(age, friends)
//
