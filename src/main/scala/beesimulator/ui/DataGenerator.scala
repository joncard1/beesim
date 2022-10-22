package beesimulator.ui

/**
  * The interface used by the UI to request a re-calculation or execution of the
  * simulator, usually because the heat influx has changed and needs to be
  * recalculated. Different implementations of the interface constitute the
  * different ways of assembling the bee population for comparison.
  */
trait DataGenerator {
  /**
    * Generates the data. The data is returned as a list so there can be
    * multiple data series per generator, as there can only be one generator for
    * the UI.
    *
    * @param heatInflux The amount of heat energy being added to the bee hive each step
    * @return           The list of data series to display.
    */
  def generatePoints(heatInflux: Double): List[Dataset]
  /**
    * The number of different families expected to be returned by the data
    * generator. Mostly a convenience so that we don't have to generate the data
    * and get the length of the list of data series when initializing the UI.
    *
    * @return The number of families of bees that the generator will provide data for
    */
  def numberOfFamilies: Int
}
