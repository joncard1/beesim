package beesimulator.controller.generators

import math.BigDecimal.double2bigDecimal
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
    // Bimodal distribution
    // Equation for the bell curve: y = (1 / (sigma*sqrt(2*pi))) * e ^((x - u)^2/(2 * sigma^2)
    // Adapted for 2 bell curves at different centers, added together.
    val bees = Vector.newBuilder[Bee[T]]
    val numberOfBees = (numberOfFathers * numberOfDaughters).toDouble
    val step = temperatureRange / (16 * 10)
    val sigma = temperatureRange / 16
    val u1 = setPoint + temperatureRange / 4
    val u2 = setPoint + 3 * temperatureRange / 4
    // The area under one of the distributions is 1, so the area under 2 is 2ish
    // (the overlap is small). The number of bees joining at a particular
    // temperature is the height, and so the total area under the curve is the
    // total number of bees in all bins times the size of the temperature bins,
    // and then by 2 (the number of curves that each add up to 1).
    val scale = (numberOfBees * step / 2) / (sigma * math.sqrt(2 * math.Pi))
    val denominator = 2 * math.pow(sigma, 2)

    for (x <- 0 to temperatureRange by step) {
      val firstNumerator = math.pow(setPoint + x.toDouble - u1, 2)
      val secondNumerator = math.pow(setPoint + x.toDouble - u2, 2)
      val exponent1 = -1 * (firstNumerator / denominator).doubleValue
      val exponent2 = -1 * (secondNumerator / denominator).doubleValue
      val numberOfBeesAtStep = scale * (math.exp(exponent1) + math.exp(exponent2))
      for (i <- 0.asInstanceOf[Int] until math.round(numberOfBeesAtStep).toInt) {
        bees += constructor(setPoint + x.toDouble)
      }
    }
    bees.result()
  }

  override def generatePoints(heatInflux: Double, heatPerBee: Double): List[Dataset] = {
    List(runSimulator(heatInflux, heatPerBee, bimodalBees, datasetCreator))
  }
}
