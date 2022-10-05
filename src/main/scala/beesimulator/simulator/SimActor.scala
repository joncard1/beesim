package beesimulator.simulator

trait SimActor[T] {
  def join(state: T): Boolean
  def leave(state: T): Boolean
}
