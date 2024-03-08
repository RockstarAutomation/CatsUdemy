package bookTasks.typeClass
import cats.Show
import cats.instances.int._
import cats.instances.string._

final case class Cat(name: String, age: Int, color: String)

class Printable[A] {
  def format[A](value: A): String = value.toString
}

object Printable {
  def format[A](value: A)(implicit printable: Printable[A]): String = format(value)

  def print[A](value: A)(implicit printable: Printable[A]): Unit = println(format(value))
}

object PrintableInstances {

  implicit val formatInt: Printable[Int] = new Printable[Int] {
    def format(value: Int): String = value.toString
  }

  implicit val formatString: Printable[String] = new Printable[String] {
    def format(value: String): String = value
  }

  implicit val formatCat: Printable[Cat] = new Printable[Cat] {
    def format(value: Cat): String = s"${value.name} is ${value.age} years old ${value.color} cat"
  }
}

object PrintableSyntax {
  implicit class PrintableOps[A](value: A) {
    def format(implicit printable: Printable[A]): String = printable.format(value)

    def print(implicit printable: Printable[A]): Unit = println(printable.format(value))
  }

}

object Test {
  def main(args: Array[String]): Unit = {

    import bookTasks.typeClass.PrintableInstances._
    import PrintableSyntax._
    Cat("Bober", 2, "Red").print

    val showInt: Show[Int] = Show.apply[Int]
    val showString: Show[String] = Show.apply[String]

    val str = showInt.show(123)
    val str1 = showString.show("321")
  }
}
