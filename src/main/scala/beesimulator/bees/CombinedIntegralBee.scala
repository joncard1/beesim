package beesimulator.bees

/** This bee is capable of eliminating steady state error, but it assumes that
  * there is a shared knowledge across the hive of the "correct" temperature.
  * This is still consistent with the Jones et al paper (2004), which discusses
  * the phenomenon that the daughters' <i>task switching</i> thresholds are
  * different, and that evolution has created a mechanism selecting for wide
  * distributions of thresholds in the hive rather than specific temperature
  * preferences, and does not preclude the possibility that there is an
  * evolutionarily selected "correct" temperature "known" to each bee. But it
  * was disappointing to find that a distributed system cannot seem to eliminate
  * steady state error. It may even be provable, though I don't think I've done
  * that strictly.
  *
  * @param threshold
  *   The task switching threshold
  * @param controlPoint
  *   The communally shared "correct" temperature. It doesn't work if this value
  *   is not shared across the population
  * @param timeConstant
  *   The time constant for the I controller term. See [[IntegralBee]] for a
  *   discussion of the parameter
  * @param accumulatedError
  *   The accumulated error across time over the life of the simulation,
  *   collected in each bee according to its experiences. On first creation,
  *   just use the default
  */
final case class CombinedIntegralBee(
    threshold: Double,
    controlPoint: Double,
    timeConstant: Double,
    accumulatedError: Double = 0
) extends Bee[InternalSimState] {
  def join(currentState: InternalSimState): Boolean = {
    val adjustedTemperature =
      currentState.currentTemperature + ((accumulatedError + currentState.currentTemperature - controlPoint) / timeConstant)
    adjustedTemperature > threshold
  }

  def leave(currentState: InternalSimState): Boolean = {
    !join(currentState)
  }
}
