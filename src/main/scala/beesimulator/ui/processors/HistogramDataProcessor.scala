package beesimulator.ui.processors

import beesimulator.ui._

// TODO: The calculation of the histogram needs to be looked at. I'm not sure
// where those peaks come from.

/**
 * This processor summarizes the collection of bees provided into a histogram
 * where each bin is a temperature range and the value of each bin is the number
 * of bees with a threshold in that range. This is a good tool for viewing the
 * distribution of the bee thresholds.
 */
object HistogramDataProcessor extends DataProcessor {
  override def transform(minimumTemperature: Double, maximumTemperature: Double, numberOfBins: Int, bees: Seq[HistogramItem]): Seq[Int] = {
    val binRange = (numberOfBins) / (maximumTemperature - minimumTemperature)
    val histogramData = new Array[Int](numberOfBins)
    bees.foreach(bee => {
      val index = math.floor((bee.value() - minimumTemperature) * binRange).toInt
      histogramData(if (index < numberOfBins) index else numberOfBins - 1) = histogramData(if (index < numberOfBins) index else numberOfBins - 1) + 1
    })

    histogramData
  }
}
