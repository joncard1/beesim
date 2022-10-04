package beesimulator.bees

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
trait Bee {
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
    * @param temperature  The temperature currently reported by the Sim.
    * @return             Whether to add the Bee to the bearding.
    */
  def join(temperature: Double) : Boolean
  /**
    * Called when a Bee is a member of a bearding to determine if it wants to
    * leave.
    *
    * @param temperature  The temperature currently reported by the Sim.
    * @return             Whether to remove the Bee to the bearding.
    */
  def leave(temperature: Double) : Boolean
  /**
    * Called after the Bee has either joined, left, or remained where in
    * whatever state it is in, giving the Bee a chance to update itself with the
    * state of the hive. Returning "this" won't break anything, but the return value is
    * Bee to give options for functional programming methods.
    *
    * @param temperature  The temperature currently reported by the Sim. This will always be the same temperature as was given to join and leave.
    * @return             The updated Bee.
    */
  def update(temperature: Double) : Bee
}

/**
  * A companion object to the Bee trait. It isn't used for much.
  */
object Bee {
  /**
    * The ordering algorithm used to sort Bee objects.
    *
    * @return An Ordering object that can be used to sort Bee objects.
    */
  implicit def simpleBeeOrdering: Ordering[SimpleBee] = Ordering.by(bee => bee.threshold)
}
