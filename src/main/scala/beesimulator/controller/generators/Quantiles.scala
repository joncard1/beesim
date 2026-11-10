package beesimulator.controller.generators

/**
  * Places a population's thresholds by quantile: to give N bees a given
  * distribution, bee k gets the threshold below which a fraction (k + 0.5) / N
  * of the distribution lies. Every bee gets a different threshold, the
  * population has exactly N bees, and a histogram of the thresholds follows
  * the distribution smoothly. Unlike drawing the thresholds at random, the
  * population is the same every time.
  */
object Quantiles {

  /**
    * The fractions of the distribution at which to place N bees: the middle of
    * each of N equal slices, (k + 0.5) / N.
    *
    * @param n The number of bees.
    * @return  The fractions, in increasing order, each strictly between 0 and 1.
    */
  def evenlySpaced(n: Int): Seq[Double] = (0 until n).map(k => (k + 0.5) / n)

  /**
    * The thresholds of N bees uniformly distributed between two temperatures.
    *
    * @param n    The number of bees.
    * @param low  The lowest temperature of the distribution.
    * @param high The highest temperature of the distribution.
    * @return     The thresholds, in increasing order.
    */
  def uniform(n: Int, low: Double, high: Double): Seq[Double] =
    evenlySpaced(n).map(p => low + p * (high - low))

  /**
    * The thresholds of N bees normally distributed.
    *
    * @param n                 The number of bees.
    * @param mean              The mean of the distribution.
    * @param standardDeviation The standard deviation of the distribution.
    * @return                  The thresholds, in increasing order.
    */
  def normal(n: Int, mean: Double, standardDeviation: Double): Seq[Double] =
    evenlySpaced(n).map(p => mean + standardDeviation * inverseStandardNormal(p))

  /**
    * The inverse of the cumulative distribution function of the standard
    * normal distribution: the value below which a fraction p of the
    * distribution lies. Uses Peter Acklam's rational approximation, which has a
    * relative error below 1.15e-9, far finer than anything the charts can show.
    *
    * @param p A fraction strictly between 0 and 1.
    * @return  The value z for which P(Z < z) = p for a standard normal Z.
    */
  def inverseStandardNormal(p: Double): Double = {
    require(p > 0 && p < 1, s"p must be strictly between 0 and 1, but was $p")
    val a = Array(-3.969683028665376e+01, 2.209460984245205e+02, -2.759285104469687e+02, 1.383577518672690e+02,
      -3.066479806614716e+01, 2.506628277459239e+00)
    val b = Array(-5.447609879822406e+01, 1.615858368580409e+02, -1.556989798598866e+02, 6.680131188771972e+01,
      -1.328068155288572e+01)
    val c = Array(-7.784894002430293e-03, -3.223964580411365e-01, -2.400758277161838e+00, -2.549732539343734e+00,
      4.374664141464968e+00, 2.938163982698783e+00)
    val d = Array(7.784695709041462e-03, 3.224671290700398e-01, 2.445134137142996e+00, 3.754408661907416e+00)
    val pLow = 0.02425

    def tail(q: Double): Double =
      (((((c(0) * q + c(1)) * q + c(2)) * q + c(3)) * q + c(4)) * q + c(5)) /
        ((((d(0) * q + d(1)) * q + d(2)) * q + d(3)) * q + 1)

    if (p < pLow) {
      tail(math.sqrt(-2 * math.log(p)))
    } else if (p > 1 - pLow) {
      -tail(math.sqrt(-2 * math.log(1 - p)))
    } else {
      val q = p - 0.5
      val r = q * q
      (((((a(0) * r + a(1)) * r + a(2)) * r + a(3)) * r + a(4)) * r + a(5)) * q /
        (((((b(0) * r + b(1)) * r + b(2)) * r + b(3)) * r + b(4)) * r + 1)
    }
  }
}
