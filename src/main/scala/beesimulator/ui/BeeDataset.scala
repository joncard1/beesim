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
  * @param indicatorBand    A range of temperatures to shade around the indicator
  *     on the bee charts, such as the range a simulation that never settled was
  *     still swinging over at the end, or None for no band
  */
final case class Dataset(
    name: String,
    indicatorPosition: Double,
    finalBeeCount: Int,
    temperatures: Seq[Double],
    bees: Seq[HistogramItem],
    indicatorBand: Option[(Double, Double)] = None
)

object Dataset {

  /**
    * The number of final steps of a simulation whose temperatures make up its
    * band. A simulation that has settled stays within a narrow band over this
    * many steps.
    */
  val SettlingWindow = 20

  /**
    * The range of temperatures over the last [[SettlingWindow]] steps.
    *
    * @param temperatures The temperature at each step, in order.
    * @return             The lowest and highest of the final temperatures.
    */
  def settlingBand(temperatures: Seq[Double]): Option[(Double, Double)] = {
    val finalTemperatures = temperatures.takeRight(SettlingWindow)
    if (finalTemperatures.isEmpty) None else Some((finalTemperatures.min, finalTemperatures.max))
  }
}
