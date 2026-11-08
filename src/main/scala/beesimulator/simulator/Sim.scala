package beesimulator.simulator

import beesimulator.bees._

/** A step of the simulation capable of generating the next step. Every step,
  * the simulator iterates over all of the actors in the simulation, sees which
  * actors are participating, and returns a simulation with a new state.
  *
  * U - the type of the force T - the type of the state V - the type of the
  * actor
  *
  * @param inactiveActors
  *   A collection of actors that are not currently participating in the
  *   simulation. This is initially all of the actors.
  * @param activeActors
  *   A collection of actors that are not currently participating in the
  *   simulation. When starting fresh, just leave the default
  * @param currentState
  *   The current state of the simulation at this step.
  */
abstract class Sim[T, U, V <: SimActor[U]](
    val inactiveActors: Seq[V],
    val activeActors: Seq[V] = Seq(),
    val currentState: U
)(implicit provider: SimActorProvider[U, V]) {

  /** A helper class that provides the step method to the [[Sim]].
    *
    * @param sim
    *   The sim to provide the step method.
    * @param provider
    *   The provider used to create a new [[Sim]]
    * @return
    *   The next step of the simulation.
    */
  implicit class SimOps[-T, +U, +V <: SimActor[U], +W <: Sim[T, U, V]](sim: W)(implicit
      provider: SimProvider[T, U, V, W]
  ) {
    def step(inForcing1: T, inForcing2: T): W = {
      val (newInactiveBees, newActiveBees, newState) =
        sim.executeStep(inForcing1, inForcing2)
      provider.createSim(newInactiveBees, newActiveBees, newState)
    }
  }

  /** Iterate the simulator one step. The simulator object returned is not the
    * same simulator the method was called on; this is to enable branching of
    * the simulation in future development.
    *
    * @param forcingValue1
    *   The energy added to the hive in this step, causing the bees to
    *   counteract it
    * @param forcingValue2
    *   The second forcing variable.
    * @return
    *   A simulator ready to iterate the next step
    */
  protected[simulator] def executeStep(
      forcingValue1: T,
      forcingValue2: T
  ): (Seq[V], Seq[V], U) = {

    var newActiveActors = Seq[V]()
    var newInactiveActors = Seq[V]()
    for (bee <- activeActors) {
      if (bee.leave(currentState)) {
        newInactiveActors = bee +: newInactiveActors
      } else {
        newActiveActors = bee +: newActiveActors
      }
    }

    for (bee <- inactiveActors) {
      if (bee.join(currentState)) {
        newActiveActors = bee +: newActiveActors
      } else {
        newInactiveActors = bee +: newInactiveActors
      }
    }

    val ns = newState(
      forcingValue1,
      forcingValue2,
      newInactiveActors.length,
      newActiveActors.length,
      currentState
    )
    val nib = newInactiveActors.map(provider.update(_, ns))
    val nab = newActiveActors.map(provider.update(_, ns))
    (nib, nab, ns)
  }

  /** Creates a new state based on which actors are participating in the
    * simulation.
    *
    * @param inForcing1
    *   The first variable trying to force change in each step of the
    *   simulation.
    * @param inForcing2
    *   The second variable trying to force change in each step of the
    *   simulation.
    * @param newInactiveBeeCount
    *   The number of actors not currently participating in the simulation.
    * @param newActiveBeeCount
    *   The number of actors currently participating in the simulation.
    * @param currentState
    *   The current state
    * @return
    *   The new state as a result of the current state, the forcing variables,
    *   and the number of participating actors.
    */
  def newState(
      inForcing1: T,
      inForcing2: T,
      newInactiveBeeCount: Int,
      newActiveBeeCount: Int,
      currentState: U
  ): U

  override def toString(): String = {
    s"Current temperature ${newState}; bees active ${activeActors.length}"
  }
}
