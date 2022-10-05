package beesimulator.bees

/**
  * A Bee that implements a PD control algorithm. It remembers the prior
  * temperature and tries to anticipate whether it should react more quickly or
  * more slowly depending on how quickly the temperature changing from the
  * threshold.
  *
  * The lookahead is in steps, meaning how many steps the Bee is looking ahead
  * based on the current rate of change and adjusting its reaction according to
  * what temperature it anticipates the hive being that many steps ahead. It is
  * similar to the standard form: u(t) = Kp*e(t) + Td*Kp*(de(t)/dt) where we
  * assume Kp to be 1, to be scaled later by @see{Sim#heatFlowPerBee}, Td is
  * represented by the variable lookahead, e(t), rather than being the
  * difference between the current temperature and a control point, is the
  * raw temperature provided to join, leave, and update as processed in a
  * complex way (see @see{SimpleBee}), and de(t)/dt is discretized as the
  * difference between the current temperature and the prior temperature.
  *
  * See [[SimpleBee]] for a discussion of Kp.
  *
  * @param threshold        This bee's threshold for joining or leaving the bearding.
  * @param priorTemperature The temperature of the prior iteration.
  * @param lookahead        A time constant representing how many simulator steps to look head.
  */
final case class DerivativeBee(threshold: Double, priorTemperature: Double, lookahead: Double) extends Bee {
    override def join(temperature: Double): Boolean = {
        val adjustedTemperature = (temperature + (lookahead * (temperature - priorTemperature)))
        (adjustedTemperature > threshold)
    }

    override def leave(temperature: Double): Boolean = {
        !join(temperature)
    }
}
