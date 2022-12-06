package beesimulator.simulator

/**
  * An actor that participates in the [[Sim]].
  */
trait SimActor[-T] {
  def join(state: T): Boolean
  def leave(state: T): Boolean
}
