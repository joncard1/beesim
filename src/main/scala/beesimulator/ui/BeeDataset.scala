package beesimulator.ui

import beesimulator.ui.processors._

/**
  * The package summarizing the data to display across all of the charts from
  * each simulator run.
  *
  * @param name             The display name of the dataset to show beneath the bee chart
  * @param finalTemperature The final temperature of the simulator, shown on the bee charts to reference where in the distribution the simulator ended up
  * @param finalBeeCount    The final count of the active bees, shown on the bee charts in text
  * @param temperatures     The data series of temperatures at each step of the simulator
  * @param bees             The entire set of bees to be summarized in the bee charts
  */
final case class Dataset(name: String, indicatorPosition: Double, finalBeeCount: Int, temperatures: Seq[Double], bees: Seq[HistogramItem])
