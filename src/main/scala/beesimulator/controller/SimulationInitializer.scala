package beesimulator.controller

import beesimulator.ui.Simulation

trait SimulationInitializer {
  def getSimulation(startTemperature: Double, temperatureRange: Double, mass: Double, specificHeat: Double): Simulation
}
