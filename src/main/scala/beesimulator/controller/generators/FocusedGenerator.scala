package beesimulator.controller.generators

import math.BigDecimal.double2bigDecimal
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
  )(implicit provider: SimProvider[Double, T, Bee[T], BeeSim[T]]) extends AbstractDataGenerator(name, numberOfIterations, simAttributes) {
  override def numberOfFamilies: Int = 1

  /**
    * The bees don't depend on the forcing values, so they are created once and
    * reused by every run.
    */
  private lazy val bees: Seq[Bee[T]] = {
    val numberOfBees = (numberOfFathers * numberOfDaughters).toDouble
    // The width of a half-bell curve that I'm going to bother to generate
    val widthInSigma = 4
    // The width in degrees of a 1-sigma difference
    val sigma = temperatureRange / (widthInSigma * (numberOfFathers + 1))
    // The width of each step in degrees. 10 steps per sigma.
    val step = sigma / 10
    val scale = (numberOfDaughters * step) / (sigma * math.sqrt(2 * math.Pi))
    val denominator = 2 * math.pow(sigma, 2)
    val mean = temperatureRange / (numberOfFathers + 1)
    val bees = for  {
      startTemperatureOfStep <- (0 to (4 * 2 * sigma) by step)
      indexOfFather <- 0.asInstanceOf[Int] until numberOfFathers
      bee <- {
        val numerator = math.pow(startTemperatureOfStep.toDouble - mean, 2)
        val exponent = -1 * numerator / denominator
        var bees = List[Bee[T]]()
        var numberOfBeesAtStep = math.round(scale * math.exp(exponent))
        for (j <- 0.asInstanceOf[Int] until numberOfBeesAtStep.toInt) {
          val threshold = setPoint + indexOfFather * mean + startTemperatureOfStep.toDouble
          bees = constructor(threshold) :: bees
        }
        bees
      }
    } yield bee
    bees
  }

  override def generatePoints(heatInflux: Double, heatPerBee: Double): List[Dataset] = {
    List(runSimulator(heatInflux, heatPerBee, bees, datasetCreator))
  }
}
