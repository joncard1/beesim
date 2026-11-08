package beesimulator

import javafx.fxml.FXMLLoader
import javafx.{fxml => jfxf, scene => jfxs}
import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.scene.Scene

import beesimulator.controller._
import beesimulator.ui.MainController

object MainJFX extends JFXApp3 {
  val StartTemperature = 20.0
  val TemperatureRange: Double = 3.0
  val Mass: Double = 5
  val SpecificHeat: Double = 0.5

  override def start(): Unit = {
    val internalSimulation =
      InternalSimulationInitializer.getSimulation(StartTemperature, TemperatureRange, Mass, SpecificHeat)
    val externalSimulation =
      ExternalSimulationInitializer.getSimulation(StartTemperature, TemperatureRange, Mass, SpecificHeat)

    stage = new JFXApp3.PrimaryStage {
      title = "Bee Simulator"

      val viewClass = getClass.getResource("ui/beecharts.fxml")
      val loader = new FXMLLoader(viewClass)
      val root: jfxs.Parent = loader.load()
      val controller = loader.getController[MainController]()
      controller.addSimulation(internalSimulation)
      controller.addSimulation(externalSimulation)
      controller.setData(StartTemperature - 0.2, StartTemperature, StartTemperature + TemperatureRange)
      scene = new Scene(root)
    }
  }

}
