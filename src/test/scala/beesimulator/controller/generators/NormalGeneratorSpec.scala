package beesimulator.controller.generators

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import beesimulator.bees._
import beesimulator.simulator._
import beesimulator.ui.Dataset
import beesimulator.controller.BeeHistogramItem

class NormalGeneratorSpec extends AnyWordSpec with Matchers {
  val state = InternalSimState(5, 0.5, 20.0)
  implicit val provider: SimProvider[Double, InternalSimState, Bee[InternalSimState], InternalBeeSim] =
    new InternalBeeSimProvider(state)

  val generator = new NormalGenerator(
    "normal",
    Seq(NormalGenerator.Group(2000, 21.5, 0.25, SimpleBee.apply)),
    state,
    (_, _, lastTemperature, name, numBees, temperatures, bees) =>
      Dataset(name, lastTemperature, numBees, temperatures, bees)
  )

  "A normal generator" should {
    "build one population with the requested size, mean and standard deviation" in {
      generator.numberOfFamilies shouldBe 1
      val datasets = generator.generatePoints(0.5, 0.001, 30)
      datasets should have size 1
      datasets.head.temperatures should have size 30
      val thresholds = datasets.head.bees.map(_.value())
      thresholds should have size 2000
      thresholds.distinct should have size 2000
      val mean = thresholds.sum / thresholds.size
      val standardDeviation = math.sqrt(thresholds.map(t => (t - mean) * (t - mean)).sum / thresholds.size)
      mean shouldBe 21.5 +- 1e-9
      standardDeviation shouldBe 0.25 +- 0.005
    }

    "build each group with its own size, distribution and kind of bee" in {
      val twoGroups = new NormalGenerator(
        "two groups",
        Seq(
          NormalGenerator.Group(300, 20.5, 0.1, SimpleBee.apply),
          NormalGenerator.Group(100, 22.5, 0.1, CombinedIntegralBee(_, 22.0, 20))
        ),
        state,
        (_, _, lastTemperature, name, numBees, temperatures, bees) =>
          Dataset(name, lastTemperature, numBees, temperatures, bees)
      )
      twoGroups.numberOfFamilies shouldBe 1
      val bees = twoGroups.generatePoints(0.5, 0.001, 10).head.bees.map(_.asInstanceOf[BeeHistogramItem].bee)
      bees should have size 400
      val (simple, integral) = bees.partition(_.isInstanceOf[SimpleBee])
      simple should have size 300
      integral should have size 100
      all(simple.map(_.threshold)) should be < 21.5
      all(integral.map(_.threshold)) should be > 21.5
      all(integral.map(_.asInstanceOf[CombinedIntegralBee].controlPoint)) shouldBe 22.0
    }
  }
}
