package beesimulator.bees

/**
  * This was an attempt to see the effects of creating a PI controller. I have
  * not come up with a way to implement an I controller element without a
  * temperature threshold shared by all the bees in the hive. This attempt,
  * using each Bee's threshold to compute the error, gives some of the
  * stabilizing effects of an I controller, but continues to have the steady-
  * state error that an I controller is supposed to have. The equilibrium
  * temperature happens when each of the active bees have thresholds below the
  * current temperature and are accumulating error indefinitely, and it's not
  * clear how that would actually effect a system where the heat flow is
  * changing; presumably the built-up error would prevent the bearding from
  * reducing the number of bees past the old equilibrium point for quite some
  * time, resulting in excessive cooling. A cap on the size of the error is
  * probably required, but that point is moot because actual bees must have some
  * form of integral control, because they do not show steady-state error.
  * 
  * The accumulated error is scaled according to the standard form, which would
  * be expressed u(t) = Kp * e(t) + Ki/Ti * e(t). The time constant here has a
  * less straight-forward meaning than in the derivative controller, but which
  * roughly means the number of steps in which we would like to remove all
  * error, and so the response is adjusted to try and achieve that.
  *
  * @param threshold    The temperature threshold of this bee.
  * @param timeConstant The time constant controlling the impact of the integral control
  * @param accumulator  The acculuated error 
  */
final case class IntegralBee(threshold: Double, timeConstant: Int, accumulator: Double = 0) extends Bee {

    override def join(temperature: Double): Boolean = {
        (temperature + (accumulator + temperature - threshold) / timeConstant) > threshold
    }

    override def leave(temperature: Double): Boolean = {
        !join(temperature)
    }
    
    override def update(temperature: Double): Bee = {
        return IntegralBee(threshold, timeConstant, accumulator + temperature - threshold)
    }
}
