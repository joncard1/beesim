package beesimulator.controller.generators

import beesimulator.bees._
import beesimulator.controller._
import beesimulator.ui._

/**
  * This generates the combination of multiple fathers, evenly spaced across the
  * temperature range, with the assumption that the daughters of each father
  * have thresholds' that are linearly distributed across the father's
  * temperature range. This is probably not a good assumpion; it's probably more
  * like [[FocusedGenerator]], where the father's child distribution is a bell-
  * curve and the combination of all of the fathers approaches a linear
  * distribution. It's even possible that the daughters of a given father will
  * <i>leave</i> the bearding if the temperature goes out of range on the top-
  * side, but the data in LOOKUP AND LOOKUP's paper is not clear on that point.
  *
  * @param name
  * @param setPoint
  * @param numberOfIterations
  * @param numberOfFathers
  * @param numberOfDaughters
  * @param fatherDifference
  * @param fatherWindow
  * @param simAttributes
  * @param constructor
  */
class MixedFathersGenerator(
    name: String,
    setPoint: Double,
    numberOfIterations: Int,
    numberOfFathers: Int,
    numberOfDaughters: Int,
    fatherDifference: Double,
    fatherWindow: Double,
    simAttributes: SimAttributes,
    constructor: (threshold: Double) => Bee = (SimpleBee.apply _)) extends AbstractDataGenerator(name, numberOfIterations, simAttributes) {
  val numberOfFamilies = 1
  
  override def generatePoints(heatInflux: Double): List[BeeDataset] = {
    val mixedFatherBees: Seq[Bee] = for {
      i <- 0.asInstanceOf[Int] to numberOfFathers
      j <- 0.asInstanceOf[Int] to numberOfDaughters
    } yield constructor(setPoint + (fatherDifference / numberOfFathers) * i + (fatherWindow / numberOfDaughters) * j)

    runSimulator(heatInflux, mixedFatherBees)
  }
}
