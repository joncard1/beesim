package beesimulator.bees

/** This is the simplest bee that raeacts to a change in the external
  * temperature.
  *
  * It had to be distinguished from [[SimpleBee]] because of the type of state
  * it is given.
  *
  * @param threshold
  */
final case class SimpleExternalBee(threshold: Double)
    extends Bee[ExternalSimState] {
  override def join(state: ExternalSimState): Boolean = {
    (state.externalTemperature > threshold)
  }

  override def leave(state: ExternalSimState): Boolean = {
    !join(state)
  }
}
