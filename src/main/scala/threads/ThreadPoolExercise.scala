package threads

import java.util.concurrent.{Executors, TimeUnit}


object ThreadPoolExercise {
  case class Task(id: Long) extends Runnable {
    override def run(): Unit = {
      println(s"Running task $id on the thread ${Thread.currentThread().getName}")
    }
  }

  class FixedThreadPool(noThreads: Int) {
    val threadPool = Executors.newFixedThreadPool(noThreads)

    def execute(runnable: Runnable): Unit = {
      threadPool.submit(runnable)
    }

    def cleanUp() : Unit = {
      threadPool.shutdown()
      threadPool.awaitTermination(5L, TimeUnit.SECONDS)
    }
  }

  def main(args: Array[String]): Unit = {
    val pool = new FixedThreadPool(10)
    (1 to 100).foreach{ i =>
      pool.execute(Task(i))
    }
    pool.cleanUp()
  }

}
