package beesimulator.bees

import org.scalatest.wordspec._
import org.scalatest.matchers.should.Matchers
import beesimulator.controller._
import beesimulator.simulator._

class InternalBeeSimSpec extends AnyWordSpec with Matchers {
  "Internal sim" when {
    "asked to run 2 steps" should {
      "stabilize at 2 bees and 1.5 degrees" in {
        val actor1 = SimpleBee(0)
        val actor2 = SimpleBee(1)
        val actor3 = SimpleBee(2)
        val actor4 = SimpleBee(3)
        val actors = Seq(actor1, actor2, actor3, actor4)
        val attributes = new InternalSimState(1, 1, 1)
        implicit val provider: SimProvider[Double, InternalSimState, Bee[InternalSimState], InternalBeeSim] = new InternalBeeSimProvider(attributes)
        val sim1 = InternalBeeSim(actors, Seq(), attributes)
        import sim1._

        val sim2 = sim1.step(1.0, 0.5)
        sim2.activeBees.length should be(1)
        sim2.currentState.currentTemperature should be(1.5)
        val sim3 = sim2.step(1, 0.5)
        sim3.activeBees.length should be(2)
        sim3.currentState.currentTemperature shouldBe (1.5)
      }
    }

    "asked to run 2 steps with more mass" should {
      "stablize at 2 bees and 1.25 degrees" in {
        val actor1 = SimpleBee(0)
        val actor2 = SimpleBee(1)
        val actor3 = SimpleBee(2)
        val actor4 = SimpleBee(3)
        val actors = Seq(actor1, actor2, actor3, actor4)
        val attributes = new InternalSimState(2, 1, 1)
        implicit val provider: SimProvider[Double, InternalSimState, Bee[InternalSimState], InternalBeeSim] = new InternalBeeSimProvider(attributes)
        val sim1 = InternalBeeSim(actors, Seq(), attributes)
        import sim1._

        val sim2 = sim1.step(1, 0.5)
        sim2.activeBees.length should be(1)
        sim2.currentState.currentTemperature should be(1.25)
        val sim3 = sim2.step(1, 0.5)
        sim3.activeBees.length should be(2)
        sim3.currentState.currentTemperature should be(1.25)
      }
    }

    "asked to run 2 steps with higher specific heat" should {
      "stablize at 2 bees and 1.25 degrees" in {
        val actor1 = SimpleBee(0)
        val actor2 = SimpleBee(1)
        val actor3 = SimpleBee(2)
        val actor4 = SimpleBee(3)
        val actors = Seq(actor1, actor2, actor3, actor4)
        val attributes = new InternalSimState(1, 2, 1)
        implicit val provider: SimProvider[Double, InternalSimState, Bee[InternalSimState], InternalBeeSim] = new InternalBeeSimProvider(attributes)
        val sim1 = InternalBeeSim(actors, Seq(), attributes)
        import sim1._

        val sim2 = sim1.step(1, 0.5)
        sim2.activeBees.length should be(1)
        sim2.currentState.currentTemperature should be(1.25)
        val sim3 = sim2.step(1, 0.5)
        sim3.activeBees.length should be(2)
        sim3.currentState.currentTemperature should be(1.25)
      }
    }

    "asked to run 2 steps starting at 2 degrees" should {
      "stablize at 2 bees and 2 degrees" in {
        val actor1 = SimpleBee(0)
        val actor2 = SimpleBee(1)
        val actor3 = SimpleBee(2)
        val actor4 = SimpleBee(3)
        val actors = Seq(actor1, actor2, actor3, actor4)
        val attributes = new InternalSimState(1, 1, 2)
        implicit val provider: SimProvider[Double, InternalSimState, Bee[InternalSimState], InternalBeeSim] = new InternalBeeSimProvider(attributes)
        val sim1 = InternalBeeSim(actors, Seq(), attributes)
        import sim1._

        val sim2 = sim1.step(1, 0.5)
        sim2.activeBees.length should be(2)
        sim2.currentState.currentTemperature should be(2)
        val sim3 = sim2.step(1, 0.5)
        sim3.activeBees.length should be(2)
        sim3.currentState.currentTemperature should be(2)
      }
    }

    "asked to run 2 steps starting at 3 degrees" should {
      "stablize at 2 bees and 2 degrees" in {
        val actor1 = SimpleBee(0)
        val actor2 = SimpleBee(1)
        val actor3 = SimpleBee(2)
        val actor4 = SimpleBee(3)
        val actors = Seq(actor1, actor2, actor3, actor4)
        val attributes = new InternalSimState(1, 1, 3)
        implicit val provider: SimProvider[Double, InternalSimState, Bee[InternalSimState], InternalBeeSim] = new InternalBeeSimProvider(attributes)
        val sim1 = InternalBeeSim(actors, Seq(), attributes)
        import sim1._

        val sim2 = sim1.step(1, 0.5)
        sim2.activeBees.length should be(3)
        sim2.currentState.currentTemperature should be(2.5)
        val sim3 = sim2.step(1, 0.5)
        sim3.activeBees.length should be(3)
        sim3.currentState.currentTemperature should be(2)
        val sim4 = sim3.step(1, 0.5)
        sim4.activeBees.length should be(2)
        sim4.currentState.currentTemperature should be(2)
      }
    }

    "asked to run 2 steps starting at 4 degrees" should {
      "stablize at 2 bees and 2 degrees" in {
        val actor1 = SimpleBee(0)
        val actor2 = SimpleBee(1)
        val actor3 = SimpleBee(2)
        val actor4 = SimpleBee(3)
        val actors = Seq(actor1, actor2, actor3, actor4)
        val attributes = new InternalSimState(1, 1, 4)
        implicit val provider: SimProvider[Double, InternalSimState, Bee[InternalSimState], InternalBeeSim] = new InternalBeeSimProvider(attributes)
        val sim1 = InternalBeeSim(actors, Seq(), attributes)
        import sim1._

        val sim2 = sim1.step(1, 0.5)
        sim2.activeBees.length should be(4)
        sim2.currentState.currentTemperature should be(3)
        val sim3 = sim2.step(1, 0.5)
        sim3.activeBees.length should be(3)
        sim3.currentState.currentTemperature should be(2.5)
        val sim4 = sim3.step(1, 0.5)
        sim4.activeBees.length should be(3)
        sim4.currentState.currentTemperature should be(2)
        val sim5 = sim4.step(1, 0.5)
        sim5.activeBees.length should be(2)
        sim5.currentState.currentTemperature should be(2)
      }
    }

    "asked to run starting at 1 degree at heat flow of 2" should {
      "stablize at 4 bees and 3.5 degrees" in {
        val actor1 = SimpleBee(0)
        val actor2 = SimpleBee(1)
        val actor3 = SimpleBee(2)
        val actor4 = SimpleBee(3)
        val actors = Seq(actor1, actor2, actor3, actor4)
        val attributes = new InternalSimState(1, 1, 1)
        implicit val provider: SimProvider[Double, InternalSimState, Bee[InternalSimState], InternalBeeSim] = new InternalBeeSimProvider(attributes)
        val sim1 = InternalBeeSim(actors, Seq(), attributes)
        import sim1._

        val sim2 = sim1.step(2, 0.5)
        sim2.activeBees.length should be(1)
        sim2.currentState.currentTemperature should be(2.5)
        val sim3 = sim2.step(2, 0.5)
        sim3.activeBees.length should be(3)
        sim3.currentState.currentTemperature should be(3.0)
        val sim4 = sim3.step(2, 0.5)
        sim4.activeBees.length should be(3)
        sim4.currentState.currentTemperature should be(3.5)
        val sim5 = sim4.step(2, 0.5)
        sim5.activeBees.length should be(4)
        sim5.currentState.currentTemperature should be(3.5)
      }
    }

    "asked to run starting at 1 degree at heat flow of 2 and tighter thresholds" should {
      "stablize at 4 bees and 2 degrees" in {
        val actor1 = SimpleBee(0)
        val actor2 = SimpleBee(0.5)
        val actor3 = SimpleBee(1)
        val actor4 = SimpleBee(1.5)
        val actor5 = SimpleBee(2)
        val actor6 = SimpleBee(2.5)
        val actor7 = SimpleBee(3)
        val actor8 = SimpleBee(3.5)
        val actor9 = SimpleBee(4)
        val actor10 = SimpleBee(4.5)
        val actor11 = SimpleBee(5)

        val actors = Seq(
          actor1,
          actor2,
          actor3,
          actor4,
          actor5,
          actor6,
          actor7,
          actor8,
          actor9,
          actor10,
          actor11
        )
        val attributes = new InternalSimState(1, 1, 1)
        implicit val provider: SimProvider[Double, InternalSimState, Bee[InternalSimState], InternalBeeSim] = new InternalBeeSimProvider(attributes)
        val sim1 = InternalBeeSim(actors, Seq(), attributes)
        import sim1._

        val sim2 = sim1.step(2, 0.5)
        sim2.activeBees.length should be(2)
        sim2.currentState.currentTemperature should be(2)
        val sim3 = sim2.step(2, 0.5)
        sim3.activeBees.length should be(4)
        sim3.currentState.currentTemperature should be(2)
      }
    }

    "asked to run starting at 1 degree at heat flow of 3 and even tighter thresholds" should {
      "oscillate between 1 and 2 degrees" in {
        val actor1 = SimpleBee(0)
        val actor2 = SimpleBee(0.25)
        val actor3 = SimpleBee(0.5)
        val actor4 = SimpleBee(0.75)
        val actor5 = SimpleBee(1)
        val actor6 = SimpleBee(1.25)
        val actor7 = SimpleBee(1.5)
        val actor8 = SimpleBee(1.75)
        val actor9 = SimpleBee(2)
        val actor10 = SimpleBee(2.25)
        val actor11 = SimpleBee(2.5)
        val actor12 = SimpleBee(2.75)
        val actor13 = SimpleBee(3.0)
        val actor14 = SimpleBee(3.25)
        val actor15 = SimpleBee(3.5)
        val actor16 = SimpleBee(3.75)
        val actor17 = SimpleBee(4)
        val actor18 = SimpleBee(4.25)
        val actor19 = SimpleBee(4.5)
        val actor20 = SimpleBee(4.75)
        val actor21 = SimpleBee(5)
        val actor22 = SimpleBee(5.25)
        val actor23 = SimpleBee(5.5)
        val actor24 = SimpleBee(5.75)
        val actor25 = SimpleBee(6)
        val actor26 = SimpleBee(6.25)
        val actor27 = SimpleBee(6.5)
        val actor28 = SimpleBee(6.75)

        val actors = Seq(
          actor1,
          actor2,
          actor3,
          actor4,
          actor5,
          actor6,
          actor7,
          actor8,
          actor9,
          actor10,
          actor11,
          actor12,
          actor13,
          actor14,
          actor15,
          actor16,
          actor17,
          actor18,
          actor19,
          actor20,
          actor21,
          actor22,
          actor23,
          actor24,
          actor25,
          actor26,
          actor27,
          actor28
        )
        val attributes = new InternalSimState(1, 1, 1)
        implicit val provider: SimProvider[Double, InternalSimState, Bee[InternalSimState], InternalBeeSim] = new InternalBeeSimProvider(attributes)
        val sim1 = InternalBeeSim(actors, Seq(), attributes)
        import sim1._

        val sim2 = sim1.step(3, 0.5)
        sim2.activeBees.length should be(4)
        sim2.currentState.currentTemperature should be(2)
        val sim3 = sim2.step(3, 0.5)
        sim3.activeBees.length should be(8)
        sim3.currentState.currentTemperature should be(1)
        val sim4 = sim3.step(3, 0.5)
        sim4.activeBees.length should be(4)
        sim4.currentState.currentTemperature should be(2)
        val sim5 = sim4.step(3, 0.5)
        sim5.activeBees.length should be(8)
        sim5.currentState.currentTemperature should be(1)
      }
    }

    "asked to run starting at 1 degree at heat flow of 2.9 and tighter thresholds" should {
      "stabilize at 6 bees at a temperature between 1.25 and 1.5" in {
        val actor1 = SimpleBee(0)
        val actor2 = SimpleBee(0.25)
        val actor3 = SimpleBee(0.5)
        val actor4 = SimpleBee(0.75)
        val actor5 = SimpleBee(1)
        val actor6 = SimpleBee(1.25)
        val actor7 = SimpleBee(1.5)
        val actor8 = SimpleBee(1.75)
        val actor9 = SimpleBee(2)
        val actor10 = SimpleBee(2.25)
        val actor11 = SimpleBee(2.5)
        val actor12 = SimpleBee(2.75)
        val actor13 = SimpleBee(3.0)
        val actor14 = SimpleBee(3.25)
        val actor15 = SimpleBee(3.5)
        val actor16 = SimpleBee(3.75)
        val actor17 = SimpleBee(4)
        val actor18 = SimpleBee(4.25)
        val actor19 = SimpleBee(4.5)
        val actor20 = SimpleBee(4.75)
        val actor21 = SimpleBee(5)
        val actor22 = SimpleBee(5.25)
        val actor23 = SimpleBee(5.5)
        val actor24 = SimpleBee(5.75)
        val actor25 = SimpleBee(6)
        val actor26 = SimpleBee(6.25)
        val actor27 = SimpleBee(6.5)
        val actor28 = SimpleBee(6.75)

        val actors = Seq(
          actor1,
          actor2,
          actor3,
          actor4,
          actor5,
          actor6,
          actor7,
          actor8,
          actor9,
          actor10,
          actor11,
          actor12,
          actor13,
          actor14,
          actor15,
          actor16,
          actor17,
          actor18,
          actor19,
          actor20,
          actor21,
          actor22,
          actor23,
          actor24,
          actor25,
          actor26,
          actor27,
          actor28
        )
        val attributes = new InternalSimState(1, 1, 1)
        implicit val provider: SimProvider[Double, InternalSimState, Bee[InternalSimState], InternalBeeSim] = new InternalBeeSimProvider(attributes)
        val sim1 = InternalBeeSim(actors, Seq(), attributes)
        import sim1._

        val sim2 = sim1.step(2.9, 0.5)
        sim2.activeBees.length should be (4)
        sim2.currentState.currentTemperature should be(1.9 +- 0.01)
        val sim3 = sim2.step(2.9, 0.5)
        sim3.activeBees.length should be (8)
        sim3.currentState.currentTemperature should be(0.8 +- 0.01)
        val sim4 = sim3.step(2.9, 0.5)
        sim4.activeBees.length should be (4)
        sim4.currentState.currentTemperature should be(1.7 +- 0.01)
        val sim5 = sim4.step(2.9, 0.5)
        sim5.activeBees.length should be (7)
        sim5.currentState.currentTemperature should be(1.1 +- 0.01)
        val sim6 = sim5.step(2.9, 0.5)
        sim6.activeBees.length should be (5)
        sim6.currentState.currentTemperature should be(1.5 +- 0.01)
        val sim7 = sim6.step(2.9, 0.5)
        sim7.activeBees.length should be (6)
        sim7.currentState.currentTemperature should be(1.4 +- 0.01)
        val sim8 = sim7.step(2.9, 0.5)
        sim8.activeBees.length should be(6)
        sim8.currentState.currentTemperature should be(1.3 +- 0.01)
        val sim9 = sim8.step(2.9, 0.5)
        sim9.activeBees.length should be(6)
        sim9.currentState.currentTemperature should be(1.2 +- 0.01)
        val sim10 = sim9.step(2.9, 0.5)
        sim10.activeBees.length should be(5)
        sim10.currentState.currentTemperature should be(1.6 +- 0.01)
        val sim11 = sim10.step(2.9, 0.5)
        sim11.activeBees.length should be(7)
        sim11.currentState.currentTemperature should be(1.0 +- 0.01)
        val sim12 = sim11.step(2.9, 0.5)
        sim12.activeBees.length should be(4) // floating point math
        sim12.currentState.currentTemperature should be(1.9 +- 0.01)
        val sim13 = sim12.step(2.9, 0.5)
        sim13.activeBees.length should be(8)
        sim13.currentState.currentTemperature should be(0.8 +- 0.01)
      }
    }
  }
}
