package beesimulator.ui.processors

import beesimulator.ui._

/**
 * This processor takes the output of the HistogramDataProcessor and re-
 * configures each column to be the cumulative count of all bees with a
 * threshold below the temperature value of each bin.
 */
object CumulativeFrequencyDataProcessor extends DataProcessor {
  override def transform(minimumYBoundary: Double, maximumYBoundary: Double, width: Int, input: Seq[HistogramItem]): Seq[Int] = {
    val histogramResults = HistogramDataProcessor.transform(minimumYBoundary, maximumYBoundary, width, input)
    assert(histogramResults.length == width)

    val cumulativeTotals = new Array[Int](width)
    // The histogram doesn't count bees below the minimum, but they are still
    // below every temperature on the chart.
    var total = input.count(_.value() < minimumYBoundary)
    for (i <- 0 until width) {
      total = total + histogramResults(i)
      cumulativeTotals(i) = total
    }

    cumulativeTotals
  }

  /**
    * Each bin's total includes the bees up to the top of the bin, so it is
    * plotted there.
    */
  override def binPosition(minimumTemperature: Double, binWidth: Double, bin: Int): Double =
    minimumTemperature + (bin + 1) * binWidth
}
