package typeclasses

import cats.{Parallel, Show}
import cats.effect.kernel.Sync
import cats.effect.std.Console
import cats.effect.{ExitCode, IO, IOApp, Ref}
import cats.implicits._

object TaglessFinalApp extends IOApp {
  case class User(username: String, age: Int)

  object User {
    implicit val showUser: Show[User] = new Show[User] {
      def show(user: User): String = s"${user.username} is ${user.age} years old"
    }
  }

  trait UserRepository[F[_]] {
    def createUser(user: User): F[Unit]

    def getUsers: F[List[User]]
  }

  object UserRepository {
    def impl[F[_]: Sync]: F[UserRepository[F]] =
      Ref.of[F, Map[String, User]](Map.empty).map { state =>
        new UserRepository[F] {
          def createUser(user: User): F[Unit] =
            state.update(_ + (user.username -> user))

          def getUsers(): F[List[User]] =
            state.get.map(_.values.toList)
        }
      }
  }

  trait UserService[F[_]] {
    def printUsers(users: List[User]): F[Unit]
  }

  object UserService {
    def impl[F[_]: Console: Parallel](): UserService[F] =
      (users: List[User]) => users.parTraverse_(user => Console[F].println(user.show))
  }
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      userRepository <- UserRepository.impl[IO]
      userService = UserService.impl[IO]()
      _ <- List(User("Alice", 42), User("Bob", 43), User("Max", 24)).parTraverse(userRepository.createUser)
      savedUsers <- userRepository.getUsers
      _ <- userService.printUsers(savedUsers)
    } yield ExitCode.Success
  }

}
