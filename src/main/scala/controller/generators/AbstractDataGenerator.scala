package beesimulator.controller.generators

import beesimulator.bees._
import beesimulator.controller._
import beesimulator.ui._

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
  protected def runSimulator(heatInflux: Double, bees: Seq[Bee]): List[BeeDataset] = {
    var sim = Sim(attributes, bees)
    var temperatures = List[Double]()

    for(i <- 1 to numberOfIterations) {
      temperatures = sim.temperature +: temperatures
      sim = sim.step(heatInflux)
    }
    temperatures = temperatures.reverse

    List(BeeDataset(name, sim.temperature, sim.activeBees.length, temperatures, bees))
  }
}
