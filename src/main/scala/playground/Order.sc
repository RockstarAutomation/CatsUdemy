case class OrderItem(desc: String, amount: Double)

case class Order(id: Int, items: List[OrderItem]) {
  def total: Double = items.map(_.amount).sum
}

def discountedPrice(order: Order): Double = {
  if (order.total > 2000) order.total * 0.8
  else order.total
}

case class Event(description: String)

class EventLog(filename: String) {
  def log(event: Event): Unit = ???
}

val items = List(OrderItem("Cellphone", 700), OrderItem("Laptop", 1400))
val order = Order(1, items)
discountedPrice(order) == 2100 * 0.8

val log = new EventLog("")

def logItemsBought(order: Order)(eventLog: EventLog): Unit = {
  order.items.foreach { _ => eventLog.log(Event("Item bought")) }
}