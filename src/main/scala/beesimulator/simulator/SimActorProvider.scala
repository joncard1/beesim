package beesimulator.simulator

/** Helper class to create new instances of a [[SimActor]] type for a specific
  * simulation.
  */
trait SimActorProvider[T, U <: SimActor[T]] {

  /** Called after the Bee has either joined, left, or remained where in
    * whatever state it is in, giving the Bee a chance to update itself with the
    * state of the hive. Returning "this" won't break anything, but the return
    * value is Bee to give options for functional programming methods.
    *
    * @param temperature
    *   The temperature currently reported by the Sim. This will always be the
    *   same temperature as was given to join and leave.
    * @return
    *   The updated Bee.
    */
  def update(actor: U, newState: T): U
}

/** Companion object that creates a new [[SimActorProvider]] by providing a
  * factory method.
  */
object SimActorProvider {
  def apply[T, U <: SimActor[T]](
      updateF: (actor: U) => U
  ): SimActorProvider[T, U] = {
    new SimActorProvider[T, U] {
      override def update(actor: U, newStat: T): U = updateF(actor)
    }
  }
}
