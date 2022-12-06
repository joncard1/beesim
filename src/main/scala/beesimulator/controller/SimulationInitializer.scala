package beesimulator.controller

import beesimulator.ui.Simulation

/** A generic interface used to initialize a [[Simulation]].
  */
trait SimulationInitializer {

  /** Creates a new [[Simulation]].
    *
    * @param startTemperature
    *   The start temperature of the simulation.
    * @param temperatureRange
    *   The range of temperatures expected by the simulation.
    * @param mass
    *   The mass of the hive.
    * @param specificHeat
    *   The specific heat of the hive.
    * @return
    *   A [[Simulation]] initialized with the provided shared constants used by
    *   all simulations.
    */
  def getSimulation(
      startTemperature: Double,
      temperatureRange: Double,
      mass: Double,
      specificHeat: Double
  ): Simulation
}
