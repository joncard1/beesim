package beesimulator.controller.generators

import beesimulator.bees._
import beesimulator.ui._
import beesimulator.controller.SimOps
import scala.annotation.tailrec

/**
  * The parent class of all of most of the data generators. The method
  * [[beesimulator.controller.generators.AbstractDataGenerator#runSimulator]]
  * runs the simulator itself so that the concrete data generators only control
  * the creation of the bees and their distributions.
  *
  * @param name
  * @param numberOfIterations
  * @param attributes
  */
abstract class AbstractDataGenerator(name: String, numberOfIterations: Int, attributes: SimAttributes) extends DataGenerator{
  import Bee._

  protected def runSimulator(heatInflux: Double, bees: Seq[Bee]): List[BeeDataset] = {
    val sim = BeeSim(attributes, bees, Seq())
    import sim._

    @tailrec
    def iterate(iteration: Int, sim: BeeSim, temperatures: List[Double]): List[Double] = {
      if (iteration == numberOfIterations) {
        temperatures
      } else {
        iterate(iteration + 1, sim.step(heatInflux), sim.currentTemperature.getOrElse(sim.attributes.initialTemperature) :: temperatures)
      }
    }
    val temperatures = iterate(0, sim, List())
    val lastTemperature = temperatures.head
    val reverseTemperatures = temperatures.reverse

    List(BeeDataset(name, lastTemperature, sim.activeBees.length, reverseTemperatures, bees))
  }
}
