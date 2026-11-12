package beesimulator.bees

import beesimulator.simulator._

/** The specific implementation of [[beesimulator.simulator.Sim]] for bees.
  *
  * @param inactiveBees
  *   The bees that are currently inactive.
  * @param activeBees
  *   The bees that are currently active.
  * @param state
  *   The state of the simulator, other than the bees.
  * @param x
  *   A class used to instantiate new instances of the simulator. This cannot be
  *   in a method in the simulator class when the class is genericized.
  */
abstract class BeeSim[T <: SimState](
    override val inactiveActors: Seq[Bee[T]],
    override val activeActors: Seq[Bee[T]],
    override val currentState: T
)(implicit x: SimActorProvider[T, Bee[T]])
    extends Sim[Double, T, Bee[T]](inactiveActors, activeActors, currentState) {}
