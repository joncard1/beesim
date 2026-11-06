package beesimulator.ui

import java.util.concurrent.Executor
import scala.util.{Failure, Success, Try}

/**
  * Runs a slow computation in the background in response to a rapidly changing
  * input, such as a slider being dragged, using a strategy called
  * "conflation": at most one computation runs at a time, and while it runs,
  * new inputs only replace a single pending input rather than queueing up. When
  * the running computation finishes, its result is delivered, and then the
  * most recent pending input (if any) is started. The inputs in between are
  * never computed at all.
  *
  * This gives the following guarantees:
  *   - Work whose result would be thrown away is never started, so the
  *     background threads never fill up with stale work.
  *   - Results are delivered in the order the computations were started,
  *     because only one is ever in flight.
  *   - The last input submitted is always computed and delivered, so the
  *     display always ends up matching the final position of the slider.
  *   - While the input is changing continuously, results are delivered as fast
  *     as they can be computed, which looks like an animation.
  *
  * The next computation does not start until the previous result has been
  * delivered. This means that if displaying a result is slow, the computations
  * slow down to match instead of piling up results waiting to be displayed.
  *
  * @param compute   The slow computation. Runs on [[workExecutor]].
  * @param onResult  Receives each result. Runs on [[deliveryExecutor]].
  * @param onIdle    Called after a result is delivered when there is no more
  *     pending input, for instance to reset a busy cursor. Runs on
  *     [[deliveryExecutor]].
  * @param onFailure Receives exceptions thrown by [[compute]]. Pending input is
  *     still processed afterwards. Runs on [[deliveryExecutor]].
  * @param workExecutor     Where [[compute]] runs; a background thread pool.
  * @param deliveryExecutor Where results are delivered; for JavaFX, this would
  *     be Platform.runLater.
  */
final class ConflatingRunner[I, O](
    compute: I => O,
    onResult: O => Unit,
    onIdle: () => Unit = () => (),
    onFailure: Throwable => Unit = e => e.printStackTrace()
)(workExecutor: Executor, deliveryExecutor: Executor) {

  /**
    * Whether a computation has been started whose result has not yet been
    * delivered.
    */
  private var running = false

  /**
    * The most recent input submitted while a computation was running.
    */
  private var pending: Option[I] = None

  /**
    * Once closed, no more results are delivered and no more computations are
    * started.
    */
  @volatile private var closed = false

  /**
    * Requests that the result for this input be computed and delivered. If a
    * computation is already running, this input replaces any input that was
    * waiting for it to finish.
    *
    * @param input The input to compute.
    */
  def submit(input: I): Unit = {
    val startNow = synchronized {
      if (closed) {
        false
      } else if (running) {
        pending = Some(input)
        false
      } else {
        running = true
        true
      }
    }
    if (startNow) {
      start(input)
    }
  }

  /**
    * Stops delivering results and discards any pending input. A computation
    * that is already running is allowed to finish, but its result is dropped.
    * Use this when the consumer of the results is going away, such as when a
    * different simulation is selected.
    */
  def close(): Unit = synchronized {
    closed = true
    pending = None
  }

  private def start(input: I): Unit = {
    workExecutor.execute(() => {
      val result = Try(compute(input))
      deliveryExecutor.execute(() => deliver(result))
    })
  }

  private def deliver(result: Try[O]): Unit = {
    if (!closed) {
      result match {
        case Success(value) => onResult(value)
        case Failure(e)     => onFailure(e)
      }
    }

    val next = synchronized {
      val next = pending
      pending = None
      running = next.isDefined
      next
    }
    next match {
      case Some(input) => start(input)
      case None        => if (!closed) onIdle()
    }
  }
}
