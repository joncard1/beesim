package beesimulator.ui

import org.scalatest.wordspec.AnyWordSpec
import rx.lang.scala.Observable
import java.util.Timer
import rx.lang.scala.Subject.apply
import rx.lang.scala.Subject
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.Callable
import rx.lang.scala.Observer

class TempSpec extends AnyWordSpec() {
  "A reactive observable" should {
    "How many subscribers can one have?" in {
      val endpoint = Subject[Int]()

      val executor = ScheduledThreadPoolExecutor(1)
      def getCallable(i: Int) = new Callable {
        def call() = {
          endpoint.onNext(i)
          throw new Exception("Done now")
        }
      }
      for (i <- 1 until 5) {
        executor.schedule(getCallable(i), i, TimeUnit.SECONDS)
      }

      val subscription1 = endpoint.subscribe(
        next => {println(s"Subscriber 1: ${next}")},
        err => {println(s"Subscriber 1 error: ${err}")},
        () => {println("Subscriber 1 completed.")})
      val subscription2 = endpoint.subscribe(
        next => {println(s"Subscriber 2: ${next}")},
        err => {println(s"Subscriber 2 error: ${err}")},
        () => {println("Subscriber 2 completed.")})

      Thread.sleep(5000)
    }

    "How many subscriptions can one have?" in {
      val endpoint1 = Subject[Int]()
      val endpoint2 = Subject[Int]()
      val endpoint3 = Observer[Int](next => {
        println(s"Received ${next}")
      })

      val subscription1 = endpoint1.subscribe(endpoint3)
      val subscription2 = endpoint2.subscribe(endpoint3)

      val executor = ScheduledThreadPoolExecutor(1)
      def getCallable(i: Int) = new Callable {
        def call() = {
          if ((i % 2) == 0) {
            endpoint1.onNext(i)
          } else {
            endpoint2.onNext(i)
          }
          throw new Exception("Done now")
        }
      }
      for (i <- 1 until 5) {
        executor.schedule(getCallable(i), i, TimeUnit.SECONDS)
      }

      Thread.sleep(5000)
    }
  }
}
