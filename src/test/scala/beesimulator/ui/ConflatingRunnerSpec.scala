package beesimulator.ui

import java.util.concurrent.{CountDownLatch, Executor, Executors, TimeUnit}
import scala.collection.mutable
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ConflatingRunnerSpec extends AnyWordSpec with Matchers {

  /**
    * An executor that holds tasks until the test runs them, so the test
    * controls exactly when each computation finishes.
    */
  class ManualExecutor extends Executor {
    val tasks = mutable.Queue[Runnable]()
    override def execute(task: Runnable): Unit = tasks.enqueue(task)
    def runNext(): Unit = tasks.dequeue().run()
  }

  /**
    * Delivers results immediately on the calling thread.
    */
  val immediate: Executor = (task: Runnable) => task.run()

  class Fixture {
    val work = new ManualExecutor()
    val computed = mutable.Buffer[Int]()
    val results = mutable.Buffer[Int]()
    val failures = mutable.Buffer[Throwable]()
    var idleCount = 0
    val runner = new ConflatingRunner[Int, Int](
      input => {
        computed += input
        if (input < 0) throw new IllegalArgumentException("negative")
        input * 10
      },
      result => results += result,
      () => idleCount += 1,
      e => failures += e
    )(work, immediate)
  }

  "A conflating runner" when {
    "given a single input" should {
      "compute it, deliver the result, and become idle" in {
        val f = new Fixture()
        f.runner.submit(1)
        f.work.tasks.size shouldBe 1
        f.work.runNext()
        f.results shouldBe Seq(10)
        f.idleCount shouldBe 1
        f.work.tasks shouldBe empty
      }
    }

    "given several inputs while busy" should {
      "skip the intermediate inputs and compute only the latest" in {
        val f = new Fixture()
        f.runner.submit(1)
        f.runner.submit(2)
        f.runner.submit(3)
        f.runner.submit(4)
        f.work.tasks.size shouldBe 1
        f.work.runNext()
        f.results shouldBe Seq(10)
        f.idleCount shouldBe 0
        f.work.tasks.size shouldBe 1
        f.work.runNext()
        f.computed shouldBe Seq(1, 4)
        f.results shouldBe Seq(10, 40)
        f.idleCount shouldBe 1
      }
    }

    "a computation fails" should {
      "report the failure and continue with the pending input" in {
        val f = new Fixture()
        f.runner.submit(-1)
        f.runner.submit(2)
        f.work.runNext()
        f.failures.size shouldBe 1
        f.work.runNext()
        f.results shouldBe Seq(20)
        f.idleCount shouldBe 1
      }
    }

    "closed while busy" should {
      "drop the running result and the pending input" in {
        val f = new Fixture()
        f.runner.submit(1)
        f.runner.submit(2)
        f.runner.close()
        f.work.runNext()
        f.results shouldBe empty
        f.work.tasks shouldBe empty
        f.runner.submit(3)
        f.work.tasks shouldBe empty
        f.idleCount shouldBe 0
      }
    }

    "given rapid inputs with real threads" should {
      "deliver results in submission order and always deliver the last input" in {
        val work = Executors.newFixedThreadPool(4)
        val delivery = Executors.newSingleThreadExecutor()
        val results = mutable.Buffer[Int]()
        val done = new CountDownLatch(1)
        val last = 10000
        val runner = new ConflatingRunner[Int, Int](
          input => { Thread.sleep(0, 100000); input },
          result => {
            results += result
            if (result == last) done.countDown()
          }
        )(work, delivery)
        try {
          for (i <- 1 to last) runner.submit(i)
          done.await(10, TimeUnit.SECONDS) shouldBe true
          results.last shouldBe last
          results shouldBe results.sorted
          results.distinct.size shouldBe results.size
          results.size should be < last
        } finally {
          work.shutdownNow()
          delivery.shutdownNow()
        }
      }
    }
  }
}
