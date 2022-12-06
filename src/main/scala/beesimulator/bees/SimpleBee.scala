package beesimulator.bees

/**
  * This is the simplest Bee. The Bee will join when the temperature is above
  * its threshold, and leave when the Bee is below its threshold.
  *
  * This represents a pure P controller (proportional controller), but whereas
  * the standard form of the PID controller is u(t) = Kp*e(t), where
  * e(t) = cp - t(t) (control point minus temperature at time t) and Kp's
  * units are (Joules removed)/(degree of error) as would be in the
  * standard form, we have: for all bti (bee i's threshold),
  * u(t) = Kp * Sigma(for all bti | bti < e(t): 1) where Kp is in Joules/bee,
  * multiplied by the count of bees whose threshold is below the current
  * temperature. The amount of error is specific to each bee, but that doesn't
  * matter because we count each bee as 1 regardless of how much error it is
  * experiencing. This makes the count of bees a proxy for e(t), but rather than
  * the response being linear, where e(t) is a constant adjustment from t(t) and
  * Kp represents a constant linear slope to u(t), the distribution of
  * thresholds of the bees provides a non-linear response to t(t), scaled by Kp
  * (which in our case is the heat flow per bee, one of the forcing variables).
  *
  * @param threshold    The temperature at which the Bee will change state.
  */
final case class SimpleBee(threshold: Double) extends Bee[InternalSimState] {
    override def join(state: InternalSimState): Boolean = {
        return (state.currentTemperature > threshold)
    }

    override def leave(state: InternalSimState): Boolean = {
        return !join(state)
    }
}
