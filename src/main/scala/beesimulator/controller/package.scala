package beesimulator

/**
 * The package that contains the simulator itself, as well as the Controller in
 * the MVC architecture of this application. This means it has dependencies on
 * the "model" in the [[beesimulator.bees]] package and the "view" in the
 * [[beesimulator.ui]] package, but they don't have dependencies on each other.
 *
 * I was tempted to make the [[Sim]] class generic in case it would be useful to
 * anyone as an exported model, without having to include the bee-specific
 * elements, but it's in Scala, so I can't define a Java module, anyway, and,
 * really, it's probably not worth it.
 */
package object controller {}
