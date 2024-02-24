case class Point(x: Double, y: Double)

def distance(p1: Point, p2: Point): Double = {
  val dx = p2.x - p1.x
  val dy = p2.y - p1.y
  math.sqrt(dx * dx + dy * dy)
}

distance(Point(1,2), Point(4,6))