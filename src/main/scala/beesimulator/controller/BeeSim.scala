package beesimulator.bees

import Bee._
import beesimulator.cosimulatorntroller._

final case class BeeSim(
    attributes: SimAttributes,
    inactiveBees: Seq[Bee],
    activeBees: Seq[Bee],
    currentTemperature: Option[Double] = None
)(implicit x: SimActorProvider[Double, Bee]) extends Sim[Double, Double, Bee](
      inactiveBees: Seq[Bee],
      activeBees: Seq[Bee],
      currentTemperature.getOrElse(attributes.initialTemperature): Double
    ) {

  implicit class BeeSimOps(sim: BeeSim) extends SimOps[Double, Double, Bee, BeeSim](sim) {
    override def createSim(inactiveBees: Seq[Bee], activeBees: Seq[Bee], newState: Double) = {
      BeeSim(attributes, inactiveBees, activeBees, Some(newState))
    }
  }

  override def newState(heatIn: Double, inactiveBeeCount: Int, activeBeeCount: Int): Double = {
        val heatOut = activeBeeCount * attributes.heatFlowPerBee

        ((currentTemperature.getOrElse(attributes.initialTemperature) * attributes.mass * attributes.specificHeat) + heatIn - heatOut) / attributes.mass / attributes.specificHeat
    }

}
