package beesimulator.bees

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import beesimulator.simulator._

class ExternalBeeSimSpec extends AnyWordSpec with Matchers {

  /**
    * A hive with mass and specific heat 1 at 20 degrees, an external
    * temperature of 22 degrees, and walls conducting 0.5 per degree of
    * difference, with bees whose thresholds are 21 and 23 degrees.
    */
  class Fixture {
    val initialState = ExternalSimState(1, 1, 20, 22, 0.5)
    implicit val provider: SimProvider[Double, ExternalSimState, Bee[ExternalSimState], ExternalBeeSim] =
      new ExternalBeeSimProvider(initialState)
    val sim = new ExternalBeeSim(Seq(SimpleExternalBee(21), SimpleExternalBee(23)), Seq(), initialState)
  }

  "External sim" when {
    "computing the next state" should {
      "add the heat conducted through the walls and remove the heat of the active bees" in {
        val f = new Fixture()
        // Heat in: 0.5 * (22 - 20) = 1. Heat out: 1 bee * 0.5 = 0.5.
        // The temperature rises by 0.5 / (mass 1 * specific heat 1).
        val state = f.sim.newState(22, 0.5, 1, 1, f.initialState)
        state.currentTemperature shouldBe 20.5
        state.externalTemperature shouldBe 22
        state.thermalConductivity shouldBe 0.5
      }
    }

    "asked to run 2 steps" should {
      "activate the bees below the external temperature and warm towards equilibrium" in {
        val f = new Fixture()
        import f.sim._
        import f.provider

        val sim2 = f.sim.step(22, 0.5)
        sim2.activeBees.length shouldBe 1
        sim2.currentState.currentTemperature shouldBe 20.5
        // Heat in: 0.5 * (22 - 20.5) = 0.75. Heat out: 0.5.
        val sim3 = sim2.step(22, 0.5)
        sim3.activeBees.length shouldBe 1
        sim3.currentState.currentTemperature shouldBe 20.75
      }
    }

    "run for many steps" should {
      "settle where the heat through the walls equals the heat the bees remove" in {
        val f = new Fixture()
        import f.sim._
        import f.provider

        val settled = (1 to 100).foldLeft(f.sim)((sim, _) => sim.step(22, 0.5))
        // 0.5 * (22 - T) = 1 bee * 0.5, so T = 21.
        settled.currentState.currentTemperature shouldBe 21.0 +- 1e-9
      }
    }
  }
}
