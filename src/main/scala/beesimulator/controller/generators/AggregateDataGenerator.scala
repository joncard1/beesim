package beesimulator.controller.generators

import scala.annotation.tailrec
import beesimulator.ui._

/**
  * A data generator for assembling multiple other generators so that the UI can
  * display multiple families, and each family can be its own class. Strictly,
  * this is the only class that has to implement [[DataGenerator]], which is a
  * UI dependency in the controller, but it doesn't really matter; the
  * dependencies are all headed the correct direction.
  */
class AggregateDataGenerator extends DataGenerator {

  /**
    * The collection of generators to operate.
    */
  private var dataGenerators: List[DataGenerator] = List()

  /**
    * Adds a generator to the collection. That generator's generated data will
    * be incorporated into the data provided by this generator to the UI.
    *
    * @param generator  The generator to add
    */
  def addDataGenerator(generator: DataGenerator): Unit = {
    dataGenerators = generator :: dataGenerators
  }

  /**
    * The total number of families from all of the sub-generators.
    *
    * @return
    */
  override def numberOfFamilies: Int = dataGenerators.foldLeft(0)((total, generator) => total + generator.numberOfFamilies)

  /**
    * Generates the data from all contained generators. All of the data from
    * each generator will get added to the collection of data series returned by
    * this generator.
    *
    * @param heatInFlux The heat being added to the hive at each step
    * @return           The data series the UI should display
    */
  override def generatePoints(heatInFlux: Double): List[BeeDataset] = {
    @tailrec
    def iterateDataGenerator(generators: List[DataGenerator], results: List[BeeDataset]): List[BeeDataset] = {
      generators match {
        case Nil => results
        case x :: xs => {
          val newValue = x.generatePoints(heatInFlux)
          iterateDataGenerator(xs, newValue ::: results)
        }
      }
    }

    iterateDataGenerator(dataGenerators, List())
  }
}
