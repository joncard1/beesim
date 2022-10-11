package beesimulator.simulator

import beesimulator.bees._

/** The simulator itself. Every step, the simulator iterates over all of the
  * bees in the hive, sees which bees are participating in the bearding, and
  * changes the total heat in the hive by the net heat flow.
  *
  * @param inactiveActors
  *   A collection of bees that are not currently part of the bearding. This is
  *   initially all of the bees in the hive
  * @param activeActors
  *   A collection of bees that were are part of the prior bearding. When
  *   starting fresh, just leave the default
  * @param temperature
  *   The current total heat in the hive. When starting fresh, leave the default
  *   and the simulator will use the initialHeat from the attributes
  *
  * U - the type of the force T - the type of the state V - the type of the
  * actor
  */
abstract class Sim[T, U, V <: SimActor[U]](
    val inactiveActors: Seq[V],
    val activeActors: Seq[V] = Seq(),
    val currentState: U
)(implicit provider: SimActorProvider[U, V]) {

  implicit class SimOps[T, U, V <: SimActor[U], W <: Sim[T, U, V]](sim: W)(
      implicit provider: SimProvider[T, U, V, W]
  ) {
    def step(inForcing: T): W = {
      val (newInactiveBees, newActiveBees, newState) =
        sim.executeStep(inForcing)
      provider.createSim(newInactiveBees, newActiveBees, newState)
    }
  }

  /** Iterate the simulator one step. The simulator object returned is not the
    * same simulator the method was called on; this is to enable branching of
    * the simulation in future development.
    *
    * @param heatIn
    *   The energy added to the hive in this step, causing the bees to
    *   counteract it
    * @return
    *   A simulator ready to iterate the next step
    */
  protected[simulator] def executeStep(heatIn: T): (Seq[V], Seq[V], U) = {

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

    val ns = newState(heatIn, newInactiveActors.length, newActiveActors.length, currentState)
    val nib = newInactiveActors.map(provider.update(_, ns))
    val nab = newActiveActors.map(provider.update(_, ns))
    (nib, nab, ns)
  }

  def newState(
      inForcing: T,
      newInactiveBeeCount: Int,
      newActiveBeeCount: Int,
      currentState: U
  ): U

  override def toString(): String = {
    s"Current temperature ${newState}; bees active ${activeActors.length}"
  }
}
