package beesimulator.controller

import beesimulator.bees._
import Bee._
import beesimulator.simulator._

/** This provider assumes the next state of the simulator is based on the heat
  * flux into the hive providing an upward force on the temperature.
  *
  * @param attributes
  */
class InternalBeeSimProvider(attributes: SimState)
    extends SimProvider[Double, SimState, Bee, InternalBeeSim] {

  override def createSim(
      newInactiveBees: Seq[Bee],
      newActiveBees: Seq[Bee],
      newState: SimState
  ): InternalBeeSim = {
    InternalBeeSim(newInactiveBees, newActiveBees, newState)
  }
}

final case class InternalBeeSim(
    inactiveBees: Seq[Bee],
    activeBees: Seq[Bee],
    override val currentState: SimState
)(implicit x: SimActorProvider[SimState, Bee])
    extends Sim[Double, SimState, Bee](
      inactiveBees: Seq[Bee],
      activeBees: Seq[Bee],
      currentState: SimState
    ) {

  override def newState(
      heatIn: Double,
      inactiveBeeCount: Int,
      activeBeeCount: Int,
      currentState: SimState
  ): SimState = {
    val heatOut = activeBeeCount * currentState.heatFlowPerBee

    SimState(
      currentState.mass,
      ((currentState.currentTemperature * currentState.mass * currentState.specificHeat) + heatIn - heatOut) / currentState.mass / currentState.specificHeat,
      currentState.specificHeat,
      currentState.heatFlowPerBee
    )
  }

}
