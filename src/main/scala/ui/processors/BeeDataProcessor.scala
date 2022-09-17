package beesimulator.ui.processors

import beesimulator.bees._

/**
  * This defines the interface for a variety of possible ways of displaying the
  * collection of bees. Processors of this kind can be spliced into the data
  * processing chain between the raw data generation and the charts.
  */
trait BeeDataProcessor {

  /**
    * This function performs the conversion from a collection of bees to a
    * sequence of integers that can be displayed. Generally, these are assumed
    * to be some kind of histogram or similar data.
    *
    * @param minimumTemperature The temperature value of the least bin.
    * @param maximumTemperature The temperature value of the greatest bin.
    * @param numberOfBins       The number of bins to use when summarizing the
    *     data. Generally, this will be the number of pixels in the chart,
    *     providing the y-value of the point.
    * @param bees               The collection of bees used to summarize with
    *     the histogram data.
    * @return                   A collection of integers in x-value order
    *     summarizing the bee dataset. Each entry is expected to be the y-value
    *     at that x-value in a histogram-like dataset.
    */
  def transform(minimumTemperature: Double, maximumTemperature: Double, numberOfBins: Int, bees: Seq[Bee]): Seq[Int]
}
