package beesimulator.controller.generators

import beesimulator.bees._
import beesimulator.ui._
import beesimulator.controller._
import beesimulator.simulator._

/**
  * This generates a distribution with 2 bell-curve shaped concentrations of
  * bees.
  *
  * This was done to see what would happen. On observing the data in the paper
  * TODO: LOOK UP REFERENCE, it appeared that there were bee families that
  * stopped participating in the bearding when the temperature got too high.
  * That may not be a correct interpretation of the data, as the y-axis was in
  * percentages not quantity of bees, but I wanted to see what would happen.
  *
  * @param name
  * @param setPoint
  * @param numberOfIterations
  * @param numberOfFathers
  * @param numberOfDaughters
  * @param temperatureRange
  * @param mass
  * @param heat
  * @param specificHeat
  */
class BimodalGenerator[T <: SimState](
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
  override val numberOfFamilies: Int = 1

  /**
    * The bees don't depend on the forcing values, so they are created once and
    * reused by every run.
    */
  private lazy val bimodalBees: Seq[Bee[T]] = {
    // Two bell curves of equal size, a quarter and three quarters of the way
    // across the temperature range, each placed by quantile so that every bee
    // has a different threshold (see Quantiles).
    val numberOfBees = numberOfFathers * numberOfDaughters
    val sigma = temperatureRange / 16
    val u1 = setPoint + temperatureRange / 4
    val u2 = setPoint + 3 * temperatureRange / 4
    val firstCurve = numberOfBees / 2
    (Quantiles.normal(firstCurve, u1, sigma) ++ Quantiles.normal(numberOfBees - firstCurve, u2, sigma)).map(constructor)
  }

  override def generatePoints(heatInflux: Double, heatPerBee: Double): List[Dataset] = {
    List(runSimulator(heatInflux, heatPerBee, bimodalBees, datasetCreator))
  }
}
