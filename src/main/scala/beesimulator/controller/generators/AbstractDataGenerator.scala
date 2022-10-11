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
abstract class AbstractDataGenerator(name: String, numberOfIterations: Int, currentState: SimState)(implicit provider: SimProvider[Double, SimState, Bee, InternalBeeSim]) extends DataGenerator{

  protected def runSimulator(heatInflux: Double, bees: Seq[Bee]): List[BeeDataset] = {
    val sim = InternalBeeSim(bees, Seq(), currentState)
    import sim._

    @tailrec
    def iterate(iteration: Int, sim: InternalBeeSim, temperatures: List[Double]): List[Double] = {
      if (iteration == numberOfIterations) {
        temperatures
      } else {
        val t = new sim.SimOps(sim)
        iterate(iteration + 1, sim.step(heatInflux), sim.currentState.currentTemperature :: temperatures)
      }
    }
    val temperatures = iterate(0, sim, List())
    val lastTemperature = temperatures.head
    val reverseTemperatures = temperatures.reverse

    List(BeeDataset(name, lastTemperature, sim.activeBees.length, reverseTemperatures, bees))
  }
}
