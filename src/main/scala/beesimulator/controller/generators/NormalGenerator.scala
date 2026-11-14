package beesimulator.controller.generators

import beesimulator.bees._
import beesimulator.controller._
import beesimulator.ui._
import beesimulator.simulator._

object NormalGenerator {

  /**
    * One normally distributed group of bees within a population.
    *
    * @param numberOfBees      The number of bees in the group.
    * @param mean              The mean of the group's thresholds.
    * @param standardDeviation The standard deviation of the group's thresholds.
    * @param constructor       Creates a bee of the group with a given
    *                          threshold, which also picks the type of bee and
    *                          any other parameters it has.
    */
  final case class Group[T](
      numberOfBees: Int,
      mean: Double,
      standardDeviation: Double,
      constructor: Double => Bee[T]
  )
}

/**
  * This generates a population made of one or more groups of bees whose
  * thresholds are normally distributed. A single group is probably the most
  * realistic shape for a real hive: whatever the distribution of each father's
  * daughters, the combination of many fathers tends towards a bell curve.
  * Several groups make populations with several peaks, such as a bimodal one,
  * or populations whose groups are different kinds of bee.
  *
  * @param name           The name of the population shown in the UI.
  * @param groups         The groups of bees that make up the population.
  * @param simAttributes  The starting state of the simulation.
  * @param datasetCreator A factory method to create the [[beesimulator.ui.Dataset]].
  */
class NormalGenerator[T <: SimState](
    name: String,
    groups: Seq[NormalGenerator.Group[T]],
    simAttributes: T,
    datasetCreator: (Double, Double, Double, String, Int, List[Double], Seq[BeeHistogramItem]) => Dataset
)(implicit provider: SimProvider[Double, T, Bee[T], BeeSim[T]])
    extends AbstractDataGenerator(name, simAttributes) {
  override val numberOfFamilies: Int = 1

  /**
    * The bees don't depend on the forcing values, so they are created once and
    * reused by every run. Each group's bees are placed by quantile so that
    * every bee in a group has a different threshold (see Quantiles).
    */
  private lazy val bees: Seq[Bee[T]] = groups.flatMap(group =>
    Quantiles.normal(group.numberOfBees, group.mean, group.standardDeviation).map(group.constructor)
  )

  override def generatePoints(heatInflux: Double, heatPerBee: Double, numberOfSteps: Int): List[Dataset] = {
    List(runSimulator(heatInflux, heatPerBee, numberOfSteps, bees, datasetCreator))
  }
}
