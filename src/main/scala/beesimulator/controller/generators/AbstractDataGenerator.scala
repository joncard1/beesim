package beesimulator.controller.generators

import beesimulator.bees._
import beesimulator.ui._
import beesimulator.controller._
import scala.annotation.tailrec
import beesimulator.simulator._

/**
  * The parent class of all of most of the data generators. The method
  * [[beesimulator.controller.generators.AbstractDataGenerator#runSimulator]]
  * runs the simulator itself so that the concrete data generators only control
  * the creation of the bees and their distributions.
  *
  * @param name
  * @param numberOfIterations
  * @param currentState
  */
abstract class AbstractDataGenerator[T <: SimState](name: String, numberOfIterations: Int, currentState: T)(implicit
    provider: SimProvider[Double, T, Bee[T], BeeSim[T]]
) extends DataGenerator {

  /**
   * This method iterates the simulation through [[numberOfInterations]], applying [[forcing1Value]] and [[forcing2Value]] at each step.
   *
    * @param forcing1Value  The first forcing value
    * @param forcing2Value  The second forcing value
    * @param bees           The collection of bees used by the simulator
    * @param datasetCreator A factory method to create the [[Dataset]].
    * @return               The dataset that resulted from the simulation.
    */
  protected def runSimulator(
      forcing1Value: Double,
      forcing2Value: Double,
      bees: Seq[Bee[T]],
      datasetCreator: (Double, Double, Double, String, Int, List[Double], Seq[BeeHistogramItem]) => Dataset
  ): Dataset = {
    val sim = provider.createSim(bees, Seq(), currentState)
    import sim._

    @tailrec
    def iterate(iteration: Int, sim: BeeSim[T], temperatures: List[Double]): (BeeSim[T], List[Double]) = {
      if (iteration == numberOfIterations) {
        (sim, temperatures)
      } else {
        iterate(
          iteration + 1,
          sim.step(forcing1Value, forcing2Value),
          sim.currentState.currentTemperature :: temperatures
        )
      }
    }
    val (finalSim, temperatures) = iterate(0, sim, List())
    // The bees in the final step decided whether to be active based on this
    // temperature, so the two are reported together.
    val lastTemperature = temperatures.head
    val reverseTemperatures = temperatures.reverse

    datasetCreator(
      forcing1Value,
      forcing2Value,
      lastTemperature,
      name,
      finalSim.activeActors.length,
      reverseTemperatures,
      bees.map(bee => BeeHistogramItem(bee))
    )
  }
}
