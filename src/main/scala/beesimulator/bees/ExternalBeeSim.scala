package beesimulator.bees

import beesimulator.simulator._

class ExternalBeeSimProvider(attributes: ExternalSimState)
    extends SimProvider[Double, ExternalSimState, Bee[ExternalSimState], ExternalBeeSim] {

  override def createSim(
      newInactiveBees: Seq[Bee[ExternalSimState]],
      newActiveBees: Seq[Bee[ExternalSimState]],
      newState: ExternalSimState
  ): ExternalBeeSim = {
    ExternalBeeSim(newInactiveBees, newActiveBees, newState)
  }
}

class ExternalBeeSim(
    inactiveBees: Seq[Bee[ExternalSimState]],
    activeBees: Seq[Bee[ExternalSimState]],
    state: ExternalSimState
)(implicit x: SimActorProvider[ExternalSimState, Bee[ExternalSimState]])
    extends BeeSim[ExternalSimState](
      inactiveBees: Seq[Bee[ExternalSimState]],
      activeBees: Seq[Bee[ExternalSimState]],
      state: ExternalSimState
    ) {

  override def newState(
      inForcing: Double,
      newInactiveBeeCount: Int,
      newActiveBeeCount: Int,
      currentState: ExternalSimState
  ): ExternalSimState = {
    val heatIn = currentState.thermalConductivity * (inForcing - currentState.currentTemperature)
    val heatOut = newActiveBeeCount * currentState.heatFlowPerBee
    val alpha = currentState.mass * currentState.specificHeat
    val newInternalTemperature = ((currentState.currentTemperature * alpha) + heatIn - heatOut) / alpha

    ExternalSimState(
      currentState.mass,
      currentState.specificHeat,
      currentState.heatFlowPerBee,
      newInternalTemperature,
      inForcing,
      currentState.thermalConductivity
    )
  }

}
