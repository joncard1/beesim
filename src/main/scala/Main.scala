package beesimulator

import beesimulator.controller._
import beesimulator.controller.generators._
import beesimulator.bees._
import beesimulator.ui._
import beesimulator.SimulatorInitializer

/**
  * The main entry point of the application, this handles the assembly of the
  * component parts and their interdependencies, which shapes the execution of
  * the application. This is where to add data generators that create different
  * strategies for the bees, and then launches the main window.
  */
object Main extends App {
  val startTemperature = 20
  val temperatureRange: Double = 3.0
  val generator = SimulatorInitializer.getGenerator(startTemperature, temperatureRange)

  val ui = new MainFrame(startTemperature - 0.2, startTemperature, startTemperature + temperatureRange, generator)
  ui.visible = true
}