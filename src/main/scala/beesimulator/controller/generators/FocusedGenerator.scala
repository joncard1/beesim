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
class FocusedGenerator(
    name: String,
    setPoint: Double,
    numberOfIterations: Int,
    numberOfFathers: Int,
    numberOfDaughters: Int,
    temperatureRange: Double,
    simAttributes: SimState)(implicit provider: SimProvider[Double, SimState, Bee, InternalBeeSim]) extends AbstractDataGenerator(name, numberOfIterations, simAttributes) {
  override def numberOfFamilies: Int = 1

  override def generatePoints(heatInflux: Double): List[BeeDataset] = {
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
        var bees = List[Bee]()
        var numberOfBeesAtStep = math.round(scale * math.exp(exponent))
        for (j <- 0.asInstanceOf[Int] until numberOfBeesAtStep.toInt) {
          val threshold = setPoint + indexOfFather * mean + startTemperatureOfStep.toDouble
          bees = SimpleBee(threshold) :: bees
        }
        bees
      }
    } yield bee

    runSimulator(heatInflux, bees)
  }
}
