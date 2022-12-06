package beesimulator.controller

import beesimulator.bees._
import beesimulator.ui.processors._

/**
 * Wrapper class so that a [[Bee]] can be summarized by a [[DataProcessor]].
 *
  * @param bee
  */
final case class BeeHistogramItem(bee: Bee[?]) extends HistogramItem {
  def value() = bee.threshold
}