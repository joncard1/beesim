package beesimulator.controller.generators

import beesimulator.bees._
import beesimulator.controller._
import beesimulator.ui._
import beesimulator.simulator._

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
class MixedFathersGenerator[T <: SimState](
    name: String,
    setPoint: Double,
    numberOfIterations: Int,
    numberOfFathers: Int,
    numberOfDaughters: Int,
    fatherDifference: Double,
    fatherWindow: Double,
    simAttributes: T,
    datasetCreator: (Double, Double, Double, String, Int, List[Double], Seq[BeeHistogramItem]) => Dataset,
    constructor: (threshold: Double) => Bee[T] = (SimpleBee.apply)
)(implicit provider: SimProvider[Double, T, Bee[T], BeeSim[T]])
    extends AbstractDataGenerator(name, numberOfIterations, simAttributes) {
  val numberOfFamilies = 1

  /**
    * The bees don't depend on the forcing values, so they are created once and
    * reused by every run.
    */
  private lazy val mixedFatherBees: Seq[Bee[T]] = for {
    i <- 0 to numberOfFathers
    // Each father's daughters are spread evenly across his window, at the
    // middles of equal slices of it (see Quantiles), so that no two bees share
    // a threshold, even with other fathers' daughters.
    lowestThreshold = setPoint + (fatherDifference / numberOfFathers) * i
    threshold <- Quantiles.uniform(numberOfDaughters + 1, lowestThreshold, lowestThreshold + fatherWindow)
  } yield constructor(threshold)

  override def generatePoints(heatInflux: Double, heatPerBee: Double): List[Dataset] = {
    List(runSimulator(heatInflux, heatPerBee, mixedFatherBees, datasetCreator))
  }
}
