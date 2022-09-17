package beesimulator.utils

import rx.lang.scala._
import scala.util.Using
import scala.util.Using.Releasable

/**
  * Used as a constructor for a Signal that just holds a constant value.
  */
object Signal {
  def apply[A](initialValue: A) = {
    new Signal(Observable.just(initialValue))
  }
}

/**
  * This turned out not to be used, but I'm not clear on whether it should be
  * somewhere. The goal was to ensure that the objects could be disposed of, but
  * the purpose the class was to be permanent repositories for the Observable
  * anyway.
  */
implicit val signalReleasable: Using.Releasable[Signal[_]] = signal => {
  signal.subscription.unsubscribe()
}

/**
  * This is based on the scala.react Signal class, which I wish had an analog in
  * the libraries that seem to be replacing it. Given an Observable, it does a
  * greedy pull on the data from the chain and caches it locally, which can then
  * be accessed with get or getOrElse, rather than having to subscribe to things
  * and have mutable variables. This object is a constant stand-in for the data
  * that's expected.
  */
class Signal[+A](ob: Observable[A]) {
  /**
    * The internal cache of the data this object stands in place of. It is
    * represented here as an Option to account for the possibility that the data
    * has not been initialized yet or an error was thrown. This is necessary
    * because the Components accessing the data are usually run by the UI
    * thread, but the data is generated in a child thread, and so the
    * initialization of this object doesn't happen nicely synchronized with the
    * first drawing of the visual components.
    */
  private var internalData: Option[A] = None
  /**
   * The function to call when the data changes. It may be hypocritical to have
   * this when I didn't want to subscribe to Observables, but this is used to
   * call out to repaint(), which then ensures that paint(Graphics2D) is called
   * in another thread.
   */
  private var onChangeHandler: A => Unit = value => {}

  /**
    * The subscription to the observable passed into the constructor so that it
    * can be unsubscribed from, if necessary.
    */
  val subscription: Subscription = ob.subscribe(value => {
    internalData = Option(value)
    if (internalData.isDefined) {
      onChangeHandler(internalData.get)
    }
  },
  err => {
    // TODO: This is setting a bad example
    internalData = None
  },
  () => {})
  def map[B](f: A => B): Signal[B] = {
    new Signal(ob.map(f))
  }

  /**
    * Similar to [[Option#isDefined]], it indicates whether there is data
    * available for use.
    *
    * @return Whether it is safe to call get.
    */
  def isDefined: Boolean = { internalData.isDefined }

  /**
    * Similar to [[Option#get]], it returns the unlifted data, or throws an
    * error if there isn't any.
    *
    * @return The data this object is standing in for.
    */
  def get: A = {
    internalData.get
  }

  /**
    * Safer version than [[get]], it will return the provided default if there
    * is not data delivered yet.
    *
    * @param default  The default returned if necessary
    * @return         The unlifted value or, if necessary, the default
    */
  def getOrElse[B >: A](default: => B): B = {
    internalData.getOrElse(default)
  }

  /**
    * Sets the change handler for this object. This function is called when the
    * Observable subscribed to delivers data.
    *
    * @param f  The onChange handler function
    */
  def onChange(f: A => Unit) = {
    onChangeHandler = f
  }
}
