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
    * Whether the toolkit could be started. It can't be on a machine with no
    * display, such as the Linux CI build, so tests that need it should be
    * skipped with `assume(JavaFxToolkit.available)`.
    */
  lazy val available: Boolean = Try {
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
