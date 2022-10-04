package beesimulator.controller

import beesimulator.bees._

/**
  * The simulator itself. Every step, the simulator iterates over all of the
  * bees in the hive, sees which bees are participating in the bearding, and
  * changes the total heat in the hive by the net heat flow.
  *
  * @param attributes   A [[beesimulator.controller.SimAttributes]] object containing the attributes of this simulator
  * @param inactiveBees A collection of bees that are not currently part of the bearding. This is initially all of the bees in the hive
  * @param activeBees   A collection of bees that were are part of the prior bearding. When starting fresh, just leave the default
  * @param heat         The current total heat in the hive. When starting fresh, leave the default and the simulator will use the initialHeat from the attributes
  */
case class Sim(attributes: SimAttributes, inactiveBees: Seq[Bee], activeBees: Seq[Bee] = Seq(), heat: Option[Double] = None) {

    /**
      * Iterate the simulator one step. The simulator object returned is not the
      * same simulator the method was called on; this is to enable branching of
      * the simulation in future development.
      *
      * @param heatIn   The energy added to the hive in this step, causing the bees to counteract it
      * @return         A simulator ready to iterate the next step
      */
    def step(heatIn: Double): Sim = {

        var newActiveBees = Seq[Bee]()
        var newInactiveBees = Seq[Bee]()
        for (bee <- activeBees) {
            if (bee.leave(temperature)) {
                newInactiveBees = bee.update(temperature) +: newInactiveBees
            } else {
                newActiveBees = bee.update(temperature) +: newActiveBees
            }
        }

        for (bee <- inactiveBees) {
            if (bee.join(temperature)) {
                newActiveBees = bee.update(temperature) +: newActiveBees
            } else {
                newInactiveBees = bee.update(temperature) +: newInactiveBees
            }
        }

        val heatOut = newActiveBees.length * attributes.heatFlowPerBee
        //println(s"$heatOut, $heatIn, ${heat.getOrElse(attributes.initialHeat)}, ${temperature}, ${newActiveBees.length}")
        return Sim(attributes, newActiveBees, newInactiveBees, Option(heat.getOrElse(attributes.initialHeat) + heatIn - heatOut))
    }

    def temperature: Double = {
        heat.getOrElse(attributes.initialHeat) / attributes.mass / attributes.specificHeat
    }

    override def toString(): String = {
        s"Current temperature ${temperature}; bees active ${activeBees.length}"
    }
}
