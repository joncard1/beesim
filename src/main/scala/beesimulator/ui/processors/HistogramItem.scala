package beesimulator.ui.processors

/**
  * A trait to mix-in an item that will be summarized by a [[DataProcessor]].
  */
trait HistogramItem {
  def value(): Double
}
