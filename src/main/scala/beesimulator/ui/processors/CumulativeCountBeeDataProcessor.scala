package beesimulator.ui.processors

import beesimulator.ui._

/**
 * This processor takes the output of the HistogramBeeDataProcessor and re-
 * configures each column to be the cumulative count of all bees with a
 * threshold below the temperature value of each bin.
 */
object CumulativeCountBeeDataProcessor extends DataProcessor {
  override def transform(minimumYBoundary: Double, maximumYBoundary: Double, width: Int, input: Seq[HistogramItem]): Seq[Int] = {
    val histogramResults = HistogramBeeDataProcessor.transform(minimumYBoundary, maximumYBoundary, width, input)
    assert(histogramResults.length == width)

    val cumulativeTotals = new Array[Int](width)
    var total = 0
    for (i <- 0 until width) {
      total = total + histogramResults(i)
      cumulativeTotals(i) = total
    }

    cumulativeTotals
  }
}
