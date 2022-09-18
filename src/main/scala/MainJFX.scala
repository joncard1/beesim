package beesimulator

import scalafx.application.JFXApp3
import scalafx.scene.paint.LinearGradient.apply
import beesimulator.ui.SimulatorScene
import beesimulator.SimulatorInitializer
import scalafx.application.Platform

object MainJFX extends JFXApp3 {
  override def start(): Unit = {
    val startTemperature = 20.0
    val temperatureRange: Double = 3.0
    val generator = SimulatorInitializer.getGenerator(startTemperature, temperatureRange)

    //javafx.application.Platform.setImplicitExit(false);
    stage = new JFXApp3.PrimaryStage{
      title = "Bee Simulator"

      scene = new SimulatorScene(startTemperature - 0.2, startTemperature, startTemperature + temperatureRange, generator)
    }
  }

}
