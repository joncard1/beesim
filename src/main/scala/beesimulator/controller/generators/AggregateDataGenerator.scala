package beesimulator.controller.generators

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import beesimulator.ui._

/**
  * A data generator for assembling multiple other generators so that the UI can
  * display multiple families, and each family can be its own class. Strictly,
  * this is the only class that has to implement [[beesimulator.ui.DataGenerator]], which is a
  * UI dependency in the controller, but it doesn't really matter; the
  * dependencies are all headed the correct direction.
  *
  * @param executionContext Where the contained generators run. They are
  *     independent of each other, so they run in parallel.
  */
class AggregateDataGenerator(executionContext: ExecutionContext = ExecutionContext.global) extends DataGenerator {

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
  override def generatePoints(heatInFlux: Double, heatPerBee: Double): List[Dataset] = {
    implicit val ec: ExecutionContext = executionContext
    // The generators are kept in reverse order of being added, and the data
    // series are returned in the order the generators were added.
    val results = Future.traverse(dataGenerators.reverse)(generator =>
      Future(generator.generatePoints(heatInFlux, heatPerBee))
    )
    Await.result(results, Duration.Inf).flatten
  }

}
