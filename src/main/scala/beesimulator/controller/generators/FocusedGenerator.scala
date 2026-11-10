package beesimulator.controller.generators

import beesimulator.bees._
import beesimulator.controller._
import beesimulator.ui._
import beesimulator.simulator._

/**
  * This generator creates multiple bell-curves, one for each father, in an even
  * spacing across the temperature range. It is similar to [[BimodalGenerator]],
  * but more evenly spaced and flexible with respect to the number of fathers.
  *
  * @param name
  * @param setPoint
  * @param numberOfIterations
  * @param numberOfFathers
  * @param numberOfDaughters
  * @param temperatureRange
  * @param simAttributes
  */
class FocusedGenerator[T <: SimState](
    name: String,
    setPoint: Double,
    numberOfIterations: Int,
    numberOfFathers: Int,
    numberOfDaughters: Int,
    temperatureRange: Double,
    simAttributes: T,
    datasetCreator: (Double, Double, Double, String, Int, List[Double], Seq[BeeHistogramItem]) => Dataset,
    constructor: (threshold: Double) => Bee[T] = (SimpleBee.apply)
)(implicit provider: SimProvider[Double, T, Bee[T], BeeSim[T]])
    extends AbstractDataGenerator(name, numberOfIterations, simAttributes) {
  override def numberOfFamilies: Int = 1

  /**
    * The bees don't depend on the forcing values, so they are created once and
    * reused by every run.
    */
  private lazy val bees: Seq[Bee[T]] = {
    // One bell curve per father, with the fathers' centres evenly spaced across
    // the temperature range and 4 standard deviations apart, each placed by
    // quantile so that every bee has a different threshold (see Quantiles).
    val spacing = temperatureRange / (numberOfFathers + 1)
    val sigma = spacing / 4
    (0 until numberOfFathers)
      .flatMap(father => Quantiles.normal(numberOfDaughters, setPoint + (father + 1) * spacing, sigma))
      .map(constructor)
  }

  override def generatePoints(heatInflux: Double, heatPerBee: Double): List[Dataset] = {
    List(runSimulator(heatInflux, heatPerBee, bees, datasetCreator))
  }
}
