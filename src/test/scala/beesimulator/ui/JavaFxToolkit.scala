package beesimulator.ui

import java.util.concurrent.{CountDownLatch, TimeUnit}
import javafx.application.Platform
import scala.concurrent.{Await, Promise}
import scala.concurrent.duration._
import scala.util.Try

/**
  * Starts the JavaFX toolkit once for all of the tests that need it, so that
  * controls can be created and laid out without showing a window.
  */
object JavaFxToolkit {

  /**
    * Settings that make JavaFX use Monocle's headless platform with software
    * rendering, so that no window or display is needed. Each can be overridden
    * on the command line, for instance -Dglass.platform=Mac to use the real
    * toolkit instead.
    */
  val HeadlessProperties = Map(
    "glass.platform" -> "Monocle",
    "monocle.platform" -> "Headless",
    "prism.order" -> "sw"
  )

  /**
    * Whether the toolkit could be started. Tests that need it should check
    * `assert(JavaFxToolkit.available)`, so that if a JavaFX upgrade breaks
    * Monocle, the tests fail rather than being silently skipped.
    */
  lazy val available: Boolean = Try {
    for ((key, value) <- HeadlessProperties if System.getProperty(key) == null) {
      System.setProperty(key, value)
    }
    val started = new CountDownLatch(1)
    Platform.startup(() => started.countDown())
    started.await(10, TimeUnit.SECONDS)
  }.recover { case _: IllegalStateException => true }.getOrElse(false)

  /**
    * Runs the code on the JavaFX thread and waits for its result.
    */
  def onFxThread[T](code: => T): T = {
    val result = Promise[T]()
    Platform.runLater(() => result.complete(Try(code)))
    Await.result(result.future, 10.seconds)
  }
}
