package beesimulator

import javafx.fxml.FXMLLoader
import javafx.{fxml => jfxf, scene => jfxs}
import scalafx.scene.SceneIncludes._
import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.stage.Screen

import beesimulator.controller._
import beesimulator.ui.{MainController, WindowSize}

/**
  * The application: creates the simulations and opens the main window.
  */
object BeeSimulatorApp extends JFXApp3 {
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
      val controller = new MainController(
        StartTemperature - 0.2,
        StartTemperature,
        StartTemperature + TemperatureRange,
        Seq(internalSimulation, externalSimulation)
      )
      loader.setController(controller)
      val root: jfxs.Parent = loader.load()
      val screen = Screen.primary.visualBounds
      val (windowWidth, windowHeight) = WindowSize.forScreen(screen.width, screen.height)
      scene = new Scene(root, windowWidth, windowHeight)
    }
  }

}
