package beesimulator.controller

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import beesimulator.bees._
import beesimulator.ui.Dataset
import beesimulator.ui.processors.HistogramDataProcessor

/**
  * Checks the results of the full simulations, as configured for the UI,
  * against the theory they demonstrate. When the hive temperature settles, the
  * heat coming in equals the heat removed by the bees, so the number of active
  * bees is heatIn / heatPerBee. For bees that simply compare the temperature to
  * their threshold, the active bees are those whose threshold is below the
  * final temperature, which is the cumulative distribution of the thresholds
  * at the final temperature. So the indicator on the cumulative chart should
  * cross the curve at heatIn / heatPerBee.
  */
class EquilibriumSpec extends AnyWordSpec with Matchers {
  val StartTemperature = 20.0
  val TemperatureRange = 3.0
  val Mass = 5.0
  val SpecificHeat = 0.5

  /**
    * The largest change in temperature over the last [[SettlingWindow]] steps
    * for the simulation to be considered settled.
    */
  val SettledRange = 0.05
  val SettlingWindow = 20

  val simulation = InternalSimulationInitializer.getSimulation(StartTemperature, TemperatureRange, Mass, SpecificHeat)

  val heatInValues = Seq(0.1, 0.5, 1.0, 2.0, 3.0)
  val heatPerBeeValues = Seq(0.001, 0.0015, 0.002)

  /**
    * Every dataset from every combination of slider values, computed once and
    * shared by the tests.
    */
  lazy val runs: Seq[(Double, Double, Dataset)] = for {
    heatIn <- heatInValues
    heatPerBee <- heatPerBeeValues
    dataset <- simulation.generator.generatePoints(heatIn, heatPerBee)
  } yield (heatIn, heatPerBee, dataset)

  def thresholds(dataset: Dataset): Seq[Double] = dataset.bees.map(_.value())

  def beeOf(dataset: Dataset): Bee[?] = dataset.bees.head.asInstanceOf[BeeHistogramItem].bee

  def isSettled(dataset: Dataset): Boolean = {
    val tail = dataset.temperatures.takeRight(SettlingWindow)
    tail.max - tail.min < SettledRange
  }

  "The internal simulation" when {
    "bees compare the temperature to their thresholds" should {
      "settle where the cumulative distribution equals heatIn / heatPerBee" in {
        val simpleRuns = runs.filter((_, _, dataset) =>
          beeOf(dataset) match {
            case _: SimpleBee | _: DerivativeBee => true
            case _                               => false
          }
        )
        val settledRuns = simpleRuns.filter((_, _, dataset) => isSettled(dataset))
        settledRuns.size should be > simpleRuns.size / 2
        for ((heatIn, heatPerBee, dataset) <- settledRuns) {
          val activeBees = thresholds(dataset).count(_ < dataset.indicatorPosition)
          val tolerance = 0.025 * dataset.bees.size
          withClue(f"${dataset.name} at heatIn=$heatIn heatPerBee=$heatPerBee: ") {
            activeBees.toDouble shouldBe (heatIn / heatPerBee) +- tolerance
          }
        }
      }
    }

    "bees share a control point" should {
      "settle at the control point when it is settled" in {
        val integralRuns = runs.filter((_, _, dataset) => beeOf(dataset).isInstanceOf[CombinedIntegralBee])
        val settledRuns = integralRuns.filter((_, _, dataset) => isSettled(dataset))
        settledRuns should not be empty
        for ((heatIn, heatPerBee, dataset) <- settledRuns) {
          val controlPoint = beeOf(dataset).asInstanceOf[CombinedIntegralBee].controlPoint
          withClue(f"${dataset.name} at heatIn=$heatIn heatPerBee=$heatPerBee: ") {
            dataset.indicatorPosition shouldBe controlPoint +- SettledRange
          }
        }
      }
    }

    "reporting the final temperatures" should {
      "band the range of the last steps, around the indicator" in {
        for ((heatIn, heatPerBee, dataset) <- runs) {
          val tail = dataset.temperatures.takeRight(Dataset.SettlingWindow)
          withClue(f"${dataset.name} at heatIn=$heatIn heatPerBee=$heatPerBee: ") {
            dataset.indicatorBand shouldBe Some((tail.min, tail.max))
            dataset.indicatorPosition should (be >= tail.min and be <= tail.max)
          }
        }
      }

      "band a wide range when the simulation never settles" in {
        val (_, _, bimodal) = runs.find((heatIn, heatPerBee, dataset) =>
          heatIn == 1.0 && heatPerBee == 0.002 && dataset.name == "Bimodal bee set"
        ).get
        val (low, high) = bimodal.indicatorBand.get
        (high - low) should be > SettledRange
      }
    }

    "reporting the final bee count" should {
      "report a non-zero count when heat is coming in" in {
        all(runs.map((_, _, dataset) => dataset.finalBeeCount)) should be > 0
      }

      "count the simple bees whose thresholds are below the final temperature" in {
        val simpleRuns = runs.filter((_, _, dataset) => beeOf(dataset).isInstanceOf[SimpleBee])
        simpleRuns should not be empty
        for ((heatIn, heatPerBee, dataset) <- simpleRuns) {
          withClue(f"${dataset.name} at heatIn=$heatIn heatPerBee=$heatPerBee: ") {
            dataset.finalBeeCount shouldBe thresholds(dataset).count(_ < dataset.indicatorPosition)
          }
        }
      }
    }

    // Known issue: when many bees have exactly the same threshold, the bin
    // containing it has a spike. In "Mixed Fathers," 41 bees share the
    // threshold 21.5, making the middle bin 160 while its neighbours are about
    // 115. The bell-curve generators have similar spikes from their thresholds
    // being generated on a coarse grid.
    "the threshold distributions are drawn as histograms" should {
      "not have isolated spikes" in {
        pendingUntilFixed {
          val (_, _, mixedFathers) = runs.find((_, _, dataset) => dataset.name == "Mixed Fathers").get
          val bins = HistogramDataProcessor.transform(StartTemperature, StartTemperature + TemperatureRange, 50, mixedFathers.bees)
          for (i <- 1 until bins.length - 1) {
            withClue(s"bin $i of ${bins.mkString(",")}: ") {
              bins(i).toDouble should be <= 1.25 * math.max(bins(i - 1), bins(i + 1))
            }
          }
        }
      }
    }
  }
}
