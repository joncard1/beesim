package beesimulator.bees

import beesimulator.controller._
import beesimulator.simulator._

/**
  * This represents the main "experimental" framework for the simulator. When
  * generating the simulator data, the simulator gives each Bee a chance to
  * either join the bearding if it is not currently part of it, or to leave the
  * bearding if it is not currently part of it. Finally, each bee is given a
  * chance to update itself with respect to the temperature. This updating
  * provides a means for the Bee to remember states, like prior temperatures.
  *
  * The join and leave methods are separated in case the Bee wants to implement
  * hysteresis (the Bee wants to leave at a lower temperature than it wants to
  * join).
  */
trait Bee[-T] extends SimActor[T] {
  /**
    * The temperature above which the Bee will join and below which the Bee will
    * leave. The implementation doesn't have to be this straightforward, but all
    * Bee objects are presumed to have a threshold and it is used for showing
    * the distribution of the Bee population in the UI.
    */
  val threshold: Double

  /**
    * Called when a Bee is not a member of a bearding to determine if it wants
    * to join.
    *
    * @param state  The state to use to decide whether to join the bearding.
    * @return       Whether to add the Bee to the bearding.
    */
  def join(state: T) : Boolean
  /**
    * Called when a Bee is a member of a bearding to determine if it wants to
    * leave.
    *
    * @param state  The state to use to decide whether to leave the bearding.
    * @return       Whether to remove the Bee to the bearding.
    */
  def leave(state: T) : Boolean
}

/**
  * A companion object to the Bee trait.
  */
object Bee {
  /**
    * The ordering algorithm used to sort Bee objects.
    *
    * @return An Ordering object that can be used to sort Bee objects.
    */
  implicit def simpleBeeOrdering: Ordering[SimpleBee] = Ordering.by(bee => bee.threshold)

  /**
    * The provider used to provision bees that sense the internal temperature.
    */
  implicit object InternalBeeSimActorProvider extends SimActorProvider[InternalSimState, Bee[InternalSimState]] {
    override def update(actor: Bee[InternalSimState], newState: InternalSimState) = {
      actor match {
        case CombinedIntegralBee(threshold, controlPoint, timeConstant, accumulatedError) =>
          CombinedIntegralBee(threshold, controlPoint, timeConstant, accumulatedError + newState.currentTemperature - controlPoint)
        case DerivativeBee(threshold, priorTemperature, lookahead) =>
          DerivativeBee(threshold, newState.currentTemperature, lookahead)
        case IntegralBee(threshold, timeConstant, accumulator) =>
          IntegralBee(threshold, timeConstant, accumulator + newState.currentTemperature - threshold)
        case e @ SimpleBee(threshold) =>
          e
      }
    }
  }

  /**
    * The provider used to provision bees that sense the external temperature.
    */
  implicit object ExteranlBeeSimActorProvider extends SimActorProvider[ExternalSimState, Bee[ExternalSimState]] {
    override def update(actor: Bee[ExternalSimState], newState: ExternalSimState): Bee[ExternalSimState] = {
      actor match {
        case e @ SimpleExternalBee(threshold) => e
      }
    }
  }
}