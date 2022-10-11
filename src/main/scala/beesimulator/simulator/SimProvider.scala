package beesimulator.simulator

trait SimProvider[T, U, V <: SimActor[U], W <: Sim[T, U, V]] {
  def createSim(newInactiveBees: Seq[V], newActiveBees: Seq[V], newState: U) : W
}
