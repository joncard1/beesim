package beesimulator.controller

import beesimulator.bees._
import beesimulator.ui.processors._

object BeeToItemConversion {

}

final case class BeeHistogramItem(bee: Bee[?]) extends HistogramItem {
  def value() = bee.threshold
}