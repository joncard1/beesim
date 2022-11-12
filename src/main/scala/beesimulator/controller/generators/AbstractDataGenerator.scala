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
abstract class AbstractDataGenerator[T <: SimState](name: String, numberOfIterations: Int, currentState: T)(implicit provider: SimProvider[Double, T, Bee[T], BeeSim[T]]) extends DataGenerator{

  protected def runSimulator(forcingValue: Double, bees: Seq[Bee[T]], datasetCreator: (Double, Double, String, Int, List[Double], Seq[BeeHistogramItem]) => Dataset): List[Dataset] = {
    val sim = provider.createSim(bees, Seq(), currentState)
    import sim._

    @tailrec
    def iterate(iteration: Int, sim: BeeSim[T], temperatures: List[Double]): List[Double] = {
      if (iteration == numberOfIterations) {
        temperatures
      } else {
        iterate(iteration + 1, sim.step(forcingValue), sim.currentState.currentTemperature :: temperatures)
      }
    }
    val temperatures = iterate(0, sim, List())
    val lastTemperature = temperatures.head
    val reverseTemperatures = temperatures.reverse

    List(datasetCreator(forcingValue, lastTemperature, name, sim.activeBees.length, reverseTemperatures, bees.map(bee => BeeHistogramItem(bee))))
  }
}
