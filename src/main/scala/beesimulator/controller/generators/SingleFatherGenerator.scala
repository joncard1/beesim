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
  * @param numberOfFathers
  * @param numberOfDaughters
  * @param fatherWindow
  * @param startingTemperture
  * @param simAttributes
  */
class SingleFatherGenerator[T <: SimState](
    name: String,
    setPoint: Double,
    numberOfFathers: Int,
    numberOfDaughters: Int,
    fatherWindow: Double,
    startingTemperture: Double,
    simAttributes: T,
    datasetCreator: (Double, Double, Double, String, Int, List[Double], Seq[BeeHistogramItem]) => Dataset,
    constructor: (threshold: Double) => Bee[T] = (SimpleBee.apply)
)(implicit provider: SimProvider[Double, T, Bee[T], BeeSim[T]])
    extends AbstractDataGenerator(name, simAttributes) {
  def numberOfFamilies: Int = 1

  /**
    * The bees don't depend on the forcing values, so they are created once and
    * reused by every run.
    */
  private lazy val bees: Seq[Bee[T]] = for {
    i: Int <- 0.asInstanceOf[Int] to ((numberOfFathers + 1) * (numberOfDaughters + 1))
  } yield constructor(startingTemperture + (fatherWindow / ((numberOfFathers + 1) * (numberOfDaughters + 1))) * i)

  def generatePoints(heatInflux: Double, heatPerBee: Double, numberOfSteps: Int): List[Dataset] = {
    List(runSimulator(heatInflux, heatPerBee, numberOfSteps, bees, datasetCreator))
  }
}
