package beesimulator.controller.generators

import beesimulator.bees._
import beesimulator.controller._
import beesimulator.ui._
import beesimulator.simulator._

/**
  * This generates a population whose thresholds are normally distributed,
  * which is probably the most realistic shape for a real hive: whatever the
  * distribution of each father's daughters, the combination of many fathers
  * tends towards a bell curve.
  *
  * @param name              The name of the population shown in the UI.
  * @param numberOfBees      The number of bees in the population.
  * @param mean              The mean of the thresholds.
  * @param standardDeviation The standard deviation of the thresholds.
  * @param simAttributes     The starting state of the simulation.
  * @param datasetCreator    A factory method to create the [[beesimulator.ui.Dataset]].
  * @param constructor       Creates a bee with a given threshold.
  */
class NormalGenerator[T <: SimState](
    name: String,
    numberOfBees: Int,
    mean: Double,
    standardDeviation: Double,
    simAttributes: T,
    datasetCreator: (Double, Double, Double, String, Int, List[Double], Seq[BeeHistogramItem]) => Dataset,
    constructor: (threshold: Double) => Bee[T] = (SimpleBee.apply)
)(implicit provider: SimProvider[Double, T, Bee[T], BeeSim[T]])
    extends AbstractDataGenerator(name, simAttributes) {
  override val numberOfFamilies: Int = 1

  /**
    * The bees don't depend on the forcing values, so they are created once and
    * reused by every run. They are placed by quantile so that every bee has a
    * different threshold (see Quantiles).
    */
  private lazy val bees: Seq[Bee[T]] = Quantiles.normal(numberOfBees, mean, standardDeviation).map(constructor)

  override def generatePoints(heatInflux: Double, heatPerBee: Double, numberOfSteps: Int): List[Dataset] = {
    List(runSimulator(heatInflux, heatPerBee, numberOfSteps, bees, datasetCreator))
  }
}
