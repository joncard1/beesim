package beesimulator.ui.processors

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class DataProcessorSpec extends AnyWordSpec with Matchers {

  final case class Item(v: Double) extends HistogramItem {
    def value(): Double = v
  }

  def items(values: Double*): Seq[HistogramItem] = values.map(Item(_))

  "The histogram processor" when {
    "given values inside the range" should {
      "count each value in the bin containing it" in {
        // Bins of width 1: [0, 1), [1, 2), [2, 3)
        HistogramDataProcessor.transform(0, 3, 3, items(0.0, 0.5, 1.0, 2.9)) shouldBe Seq(2, 1, 1)
      }
    }

    "given a value exactly on the maximum" should {
      "count it in the last bin" in {
        HistogramDataProcessor.transform(0, 3, 3, items(3.0)) shouldBe Seq(0, 0, 1)
      }
    }

    // Known issue: values above the range are piled into the last bin, which
    // shows up as a spike at the right edge of the chart.
    "given a value above the maximum" should {
      "not count it in any bin" in {
        pendingUntilFixed {
          HistogramDataProcessor.transform(0, 3, 3, items(5.0)) shouldBe Seq(0, 0, 0)
        }
      }
    }

    // Known issue: a value below the range throws an
    // ArrayIndexOutOfBoundsException.
    "given a value below the minimum" should {
      "not count it in any bin" in {
        pendingUntilFixed {
          HistogramDataProcessor.transform(0, 3, 3, items(-1.0)) shouldBe Seq(0, 0, 0)
        }
      }
    }
  }

  "The cumulative frequency processor" when {
    "given values inside the range" should {
      "end at the total number of values" in {
        val values = items(0.0, 0.5, 1.0, 2.9)
        val result = CumulativeFrequencyDataProcessor.transform(0, 3, 3, values)
        result shouldBe Seq(2, 3, 4)
        result.last shouldBe values.size
      }
    }
  }
}
