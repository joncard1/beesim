package beesimulator.bees

import beesimulator.bees._
import Bee._
import beesimulator.simulator._

/**
 * The provider that creates new instances of [[InternalBeeSim]].
 *
 * This provider assumes the next state of the simulator is based on the heat
 * flux into the hive providing an upward force on the temperature.
 *
 * @param attributes
 */
class InternalBeeSimProvider(attributes: InternalSimState)
    extends SimProvider[Double, InternalSimState, Bee[InternalSimState], InternalBeeSim] {

  override def createSim(
      newInactiveBees: Seq[Bee[InternalSimState]],
      newActiveBees: Seq[Bee[InternalSimState]],
      newState: InternalSimState
  ): InternalBeeSim = {
    InternalBeeSim(newInactiveBees, newActiveBees, newState)
  }

}

/**
 * The simulator for bees that sense the internal temperature. The forcing
 * variables are the external temperature and the heat dissipated per bee.
 *
 * @param inactiveBees  The inactive bees
 * @param activeBees    The active bees
 * @param currentState  The current state
 * @param x             The provider used to create new instances of the simulator.
 */
final case class InternalBeeSim(
    override val inactiveActors: Seq[Bee[InternalSimState]],
    override val activeActors: Seq[Bee[InternalSimState]],
    override val currentState: InternalSimState
)(implicit x: SimActorProvider[InternalSimState, Bee[InternalSimState]])
    extends BeeSim[InternalSimState](
      inactiveActors: Seq[Bee[InternalSimState]],
      activeActors: Seq[Bee[InternalSimState]],
      currentState: InternalSimState
    ) {

  override def newState(
      heatIn: Double,
      heatPerBee: Double,
      inactiveBeeCount: Int,
      activeBeeCount: Int,
      currentState: InternalSimState
  ): InternalSimState = {
    val heatOut = activeBeeCount * heatPerBee
    val alpha = currentState.mass * currentState.specificHeat

    InternalSimState(
      currentState.mass,
      currentState.specificHeat,
      ((currentState.currentTemperature * alpha) + heatIn - heatOut) / alpha
    )
  }

}
