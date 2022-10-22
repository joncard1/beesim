package beesimulator.bees

final case class SimpleExternalBee(threshold: Double) extends Bee[ExternalSimState] {
  override def join(state: ExternalSimState): Boolean = {
    (state.externalTemperature > threshold)
  }

  override def leave(state: ExternalSimState): Boolean = {
    !join(state)
  }
}
