package beesimulator

import javafx.fxml.FXMLLoader
import javafx.{fxml => jfxf, scene => jfxs}
import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.scene.Scene

import beesimulator.SimulatorInitializer
import beesimulator.ui.MainController

object MainJFX extends JFXApp3 {
  override def start(): Unit = {
    val startTemperature = 20.0
    val temperatureRange: Double = 3.0
    val generator = SimulatorInitializer.getGenerator(startTemperature, temperatureRange)

    stage = new JFXApp3.PrimaryStage{
      title = "Bee Simulator"

      val viewClass = getClass.getResource("ui/beecharts.fxml")
      val loader = new FXMLLoader(getClass.getResource("ui/beecharts.fxml"))
      val root: jfxs.Parent = loader.load()
      val controller = loader.getController[MainController]()
      controller.setData(startTemperature - 0.2, startTemperature, startTemperature + temperatureRange, generator)
      scene = new Scene(root)
      root.getStylesheets().add("beesimulator/ui/beechart.css")
    }
  }

}
