package beesimulator.controller.generators

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import beesimulator.bees._
import beesimulator.simulator._
import beesimulator.ui.Dataset

class NormalGeneratorSpec extends AnyWordSpec with Matchers {
  val state = InternalSimState(5, 0.5, 20.0)
  implicit val provider: SimProvider[Double, InternalSimState, Bee[InternalSimState], InternalBeeSim] =
    new InternalBeeSimProvider(state)

  val generator = new NormalGenerator(
    "normal",
    2000,
    21.5,
    0.25,
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
  }
}
