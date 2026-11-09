package beesimulator.ui

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class AxisScaleSpec extends AnyWordSpec with Matchers {
  "An axis scale" when {
    "given a maximum" should {
      "use ticks of 1, 2 or 5 times a power of ten, with the bound at or above the maximum" in {
        AxisScale.forMaximum(160) shouldBe (200.0, 50.0)
        AxisScale.forMaximum(205) shouldBe (250.0, 50.0)
        AxisScale.forMaximum(3321) shouldBe (4000.0, 1000.0)
        AxisScale.forMaximum(10) shouldBe (10.0, 2.0)
        val (upperBound, tick) = AxisScale.forMaximum(1)
        upperBound shouldBe 1.0 +- 1e-12
        tick shouldBe 0.2 +- 1e-12
      }
    }

    "given a maximum of zero" should {
      "still give a usable axis" in {
        AxisScale.forMaximum(0) shouldBe (1.0, 1.0)
      }
    }
  }
}
