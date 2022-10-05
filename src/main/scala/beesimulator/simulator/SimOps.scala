package beesimulator.simulator

abstract class SimOps[T, U, V <: SimActor[U], W <: Sim[T, U, V]](sim: W) {
  def createSim(newInactiveBees: Seq[V], newActiveBees: Seq[V], newState: U) : W
  def step(inForcing: T): W = {
    val (newInactiveBees, newActiveBees, newState) = sim.executeStep(inForcing)
    createSim(newInactiveBees, newActiveBees, newState)
  }
}
