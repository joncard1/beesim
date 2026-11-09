package beesimulator.controller.generators

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class QuantilesSpec extends AnyWordSpec with Matchers {
  "The inverse standard normal" when {
    "given standard fractions" should {
      "match published values" in {
        Quantiles.inverseStandardNormal(0.5) shouldBe 0.0 +- 1e-9
        Quantiles.inverseStandardNormal(0.975) shouldBe 1.959963985 +- 1e-8
        Quantiles.inverseStandardNormal(0.8413447461) shouldBe 1.0 +- 1e-8
        Quantiles.inverseStandardNormal(0.001) shouldBe -3.090232306 +- 1e-8
        Quantiles.inverseStandardNormal(0.02) shouldBe -2.053748911 +- 1e-8
      }

      "be symmetric and increasing" in {
        val fractions = Quantiles.evenlySpaced(1000)
        val values = fractions.map(Quantiles.inverseStandardNormal)
        values shouldBe sorted
        for (p <- fractions) {
          Quantiles.inverseStandardNormal(p) shouldBe -Quantiles.inverseStandardNormal(1 - p) +- 1e-9
        }
      }
    }

    "given a fraction outside (0, 1)" should {
      "reject it" in {
        an[IllegalArgumentException] should be thrownBy Quantiles.inverseStandardNormal(0)
        an[IllegalArgumentException] should be thrownBy Quantiles.inverseStandardNormal(1)
      }
    }
  }

  "Evenly spaced fractions" should {
    "be the middles of equal slices" in {
      Quantiles.evenlySpaced(4) shouldBe Seq(0.125, 0.375, 0.625, 0.875)
    }
  }

  "Uniform thresholds" should {
    "be spread evenly between the bounds" in {
      Quantiles.uniform(3, 20.0, 21.5) shouldBe Seq(20.25, 20.75, 21.25)
    }
  }

  "Normal thresholds" should {
    "have the requested mean and standard deviation" in {
      val thresholds = Quantiles.normal(2000, 21.0, 0.2)
      thresholds should have size 2000
      thresholds.distinct should have size 2000
      val mean = thresholds.sum / thresholds.size
      val standardDeviation = math.sqrt(thresholds.map(t => (t - mean) * (t - mean)).sum / thresholds.size)
      mean shouldBe 21.0 +- 1e-9
      standardDeviation shouldBe 0.2 +- 0.005
    }
  }
}
