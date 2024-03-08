package bookTasks.typeClass

import cats.implicits.catsSyntaxEq
import cats.{Eq, Show}
import cats.instances.int._
import cats.instances.string._
import cats.syntax.show._

import java.util.Date
import cats.instances.long._

object ShowInstances {
  implicit val catShow: Show[Cat] = Show.show[Cat] { cat =>
    val name = cat.name.show
    val age = cat.age.show
    val color = cat.color.show
    s"$name is a $age year-old $color cat."
  }

  implicit val dateEq: Eq[Date] =
    Eq.instance[Date] { (date1, date2) =>
      date1.getTime === date2.getTime
    }

  implicit val catEq: Eq[Cat] =
    Eq.instance[Cat] { (cat1, cat2) =>
      cat1.color === cat2.color &&
        cat1.name === cat2.name &&
        cat1.age === cat2.age
    }
}


object TestShow {
  def main(args: Array[String]): Unit = {
    import ShowInstances._
    //    println(Cat("Bober", 2, "Red").show)

    // DATE
//    val x = new Date() // now
//    Thread.sleep(1000)
//    val y = new Date() // a bit later than now
//    println(x === x)
//    println(x === y)

    // CATS
    val cat1 = Cat("Garfield",   38, "orange and black")
    val cat2 = Cat("Heathcliff", 33, "orange and black")
    println(cat1 === cat2)

    val optionCat1 = Option(cat1)
    val optionCat2 = Option.empty[Cat]
    println(optionCat1 === optionCat2)
  }
}
