package beesimulator.simulator

/**
  * Helper class to create new instances of a [[Sim]] subtype.
  */
trait SimProvider[+T, -U, -V <: SimActor[U], +W <: Sim[T, U, V]] {
  def createSim(newInactiveBees: Seq[V], newActiveBees: Seq[V], newState: U): W
}
