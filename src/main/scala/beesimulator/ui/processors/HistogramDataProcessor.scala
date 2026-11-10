package beesimulator.ui.processors

import beesimulator.ui._

/**
 * This processor summarizes the collection of bees provided into a histogram
 * where each bin is a temperature range and the value of each bin is the number
 * of bees with a threshold in that range. This is a good tool for viewing the
 * distribution of the bee thresholds.
 */
object HistogramDataProcessor extends DataProcessor {

  /**
    * Counts the bees in each bin. Bees outside the range from the minimum to
    * the maximum temperature are not counted; a bee exactly at the maximum is
    * counted in the last bin.
    */
  override def transform(
      minimumTemperature: Double,
      maximumTemperature: Double,
      numberOfBins: Int,
      bees: Seq[HistogramItem]
  ): Seq[Int] = {
    val binRange = (numberOfBins) / (maximumTemperature - minimumTemperature)
    val histogramData = new Array[Int](numberOfBins)
    bees.foreach(bee => {
      if (bee.value() >= minimumTemperature && bee.value() <= maximumTemperature) {
        val index = math.min(math.floor((bee.value() - minimumTemperature) * binRange).toInt, numberOfBins - 1)
        histogramData(index) = histogramData(index) + 1
      }
    })

    histogramData
  }

  /**
    * Each bin is plotted at its middle.
    */
  override def binPosition(minimumTemperature: Double, binWidth: Double, bin: Int): Double =
    minimumTemperature + (bin + 0.5) * binWidth
}
