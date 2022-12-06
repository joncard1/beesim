package beesimulator.bees

import beesimulator.simulator._

/**
  * The provider to instantiate new instances of the [[ExternalBeeSim]].
  *
  * @param state
  */
class ExternalBeeSimProvider(state: ExternalSimState)
    extends SimProvider[Double, ExternalSimState, Bee[
      ExternalSimState
    ], ExternalBeeSim] {

  override def createSim(
      newInactiveBees: Seq[Bee[ExternalSimState]],
      newActiveBees: Seq[Bee[ExternalSimState]],
      newState: ExternalSimState
  ): ExternalBeeSim = {
    ExternalBeeSim(newInactiveBees, newActiveBees, newState)
  }
}

/**
 * The subclass of [[BeeSim]] for bees that sense the external temperature
 * rather than the internal one.
 *
 * @param inactiveBees The bees that are inactive.
 * @param activeBees   The bees that are active.
 * @param state        The current state of the simulator.
 * @param x            A provider to instantiate new instances of the simulator.
 */
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
      externalTemperature: Double,
      heatFlowPerBee: Double,
      newInactiveBeeCount: Int,
      newActiveBeeCount: Int,
      currentState: ExternalSimState
  ): ExternalSimState = {
    val heatIn =
      currentState.thermalConductivity * (externalTemperature - currentState.currentTemperature)
    val heatOut = newActiveBeeCount * heatFlowPerBee
    val alpha = currentState.mass * currentState.specificHeat
    val newInternalTemperature =
      ((currentState.currentTemperature * alpha) + heatIn - heatOut) / alpha

    ExternalSimState(
      currentState.mass,
      currentState.specificHeat,
      newInternalTemperature,
      externalTemperature,
      currentState.thermalConductivity
    )
  }

}
