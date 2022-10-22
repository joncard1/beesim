package beesimulator.bees

import beesimulator.simulator._

abstract class BeeSim[T <: SimState](val inactiveBees: Seq[Bee[T]], val activeBees: Seq[Bee[T]], val state: T)
  (implicit x: SimActorProvider[T, Bee[T]]) extends Sim[Double, T, Bee[T]](inactiveBees, activeBees, state) {

}
