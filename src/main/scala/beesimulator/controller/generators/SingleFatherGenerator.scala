package beesimulator.controller.generators

import beesimulator.bees._
import beesimulator.controller._
import beesimulator.ui._
import beesimulator.simulator._

/**
  * A simple generator that creates daughters from one father whose distribution
  * of thresholds' across his daughters is linear. As described in
  * [[MixedFathersGenerator]], this linear distribution may not be a good
  * assumption.
  *
  * @param name
  * @param setPoint
  * @param numberOfIterations
  * @param numberOfFathers
  * @param numberOfDaughters
  * @param fatherWindow
  * @param startingTemperture
  * @param simAttributes
  */
class SingleFatherGenerator(
    name: String,
    setPoint: Double,
    numberOfIterations: Int,
    numberOfFathers: Int,
    numberOfDaughters: Int,
    fatherWindow: Double,
    startingTemperture: Double,
    simAttributes: SimState)(implicit provider: SimProvider[Double, SimState, Bee, InternalBeeSim]) extends AbstractDataGenerator(name, numberOfIterations, simAttributes) {
  def numberOfFamilies: Int = 1

  def generatePoints(heatInflux: Double): List[BeeDataset] = {
    val bees: Seq[Bee] = for {
      i: Int <- 0.asInstanceOf[Int] to ((numberOfFathers + 1) * (numberOfDaughters + 1))
    } yield SimpleBee(startingTemperture + (fatherWindow / ((numberOfFathers + 1) * (numberOfDaughters + 1))) * i)

    runSimulator(heatInflux, bees)
  }
}
