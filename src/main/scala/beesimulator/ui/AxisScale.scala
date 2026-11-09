package beesimulator.ui

/**
  * Chooses a readable scale for a numeric axis that starts at zero, for axes
  * that can't range automatically, such as the y-axes shared by the bee charts.
  */
object AxisScale {

  /**
    * The approximate number of ticks to aim for.
    */
  val TargetTicks = 5

  /**
    * Chooses an upper bound and tick spacing for an axis from zero to at least
    * the given maximum. The tick spacing is 1, 2 or 5 times a power of ten,
    * and the upper bound is the first multiple of it at or above the maximum.
    *
    * @param maximum The largest value the axis has to show.
    * @return        The upper bound and the tick spacing.
    */
  def forMaximum(maximum: Double): (Double, Double) = {
    if (maximum <= 0) {
      (1.0, 1.0)
    } else {
      val roughTick = maximum / TargetTicks
      val magnitude = math.pow(10, math.floor(math.log10(roughTick)))
      val tick = Seq(1.0, 2.0, 5.0, 10.0).map(_ * magnitude).find(_ >= roughTick).get
      (math.ceil(maximum / tick) * tick, tick)
    }
  }
}
