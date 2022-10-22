package beesimulator.bees

import beesimulator.bees._
import Bee._
import beesimulator.simulator._

/** This provider assumes the next state of the simulator is based on the heat
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

final case class InternalBeeSim(
    override val inactiveBees: Seq[Bee[InternalSimState]],
    override val activeBees: Seq[Bee[InternalSimState]],
    override val currentState: InternalSimState
)(implicit x: SimActorProvider[InternalSimState, Bee[InternalSimState]])
    extends BeeSim[InternalSimState](
      inactiveBees: Seq[Bee[InternalSimState]],
      activeBees: Seq[Bee[InternalSimState]],
      currentState: InternalSimState
    ) {

  override def newState(
      heatIn: Double,
      inactiveBeeCount: Int,
      activeBeeCount: Int,
      currentState: InternalSimState
  ): InternalSimState = {
    val heatOut = activeBeeCount * currentState.heatFlowPerBee
    val alpha = currentState.mass * currentState.specificHeat

    InternalSimState(
      currentState.mass,
      currentState.specificHeat,
      currentState.heatFlowPerBee,
      ((currentState.currentTemperature * alpha) + heatIn - heatOut) / alpha
    )
  }

}
