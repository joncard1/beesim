package beesimulator.ui

import java.awt.image.BufferedImage
import java.io.File
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.scene.control.{ComboBox, Slider}
import javax.imageio.ImageIO
import beesimulator.controller.{ExternalSimulationInitializer, InternalSimulationInitializer}

/**
  * Renders the screenshots used in README.md, headlessly, from the real main
  * view. Run with:
  *
  *   sbt "Test/runMain beesimulator.ui.ReadmeScreenshots docs/images"
  *
  * Rerun it when the UI changes so the README stays accurate.
  */
object ReadmeScreenshots {
  val Width = 1440.0
  val Height = 900.0

  // The application's hive constants.
  val StartTemperature = 20.0
  val TemperatureRange = 3.0
  val Mass = 5.0
  val SpecificHeat = 0.5

  final case class Shot(
      file: String,
      simulation: Int,
      summary: MainController.ComboSelections,
      heatIn: Double,
      heatPerBee: Double
  )

  val shots = Seq(
    Shot("histogram.png", 0, MainController.ComboSelections.Histogram, 1.0, 0.001),
    Shot("cumulative.png", 0, MainController.ComboSelections.Cumulative, 1.0, 0.002),
    Shot("external.png", 1, MainController.ComboSelections.Cumulative, 21.5, 0.001)
  )

  def main(args: Array[String]): Unit = {
    val directory = new File(args.headOption.getOrElse("docs/images"))
    directory.mkdirs()
    require(JavaFxToolkit.available, "JavaFX could not start")
    for (shot <- shots) {
      val file = new File(directory, shot.file)
      ImageIO.write(render(shot), "png", file)
      println(s"Wrote $file")
    }
    javafx.application.Platform.exit()
  }

  private def render(shot: Shot): BufferedImage = {
    val root = JavaFxToolkit.onFxThread {
      val loader = new FXMLLoader(classOf[MainController].getResource("beecharts.fxml"))
      val controller = MainController(StartTemperature - 0.2, StartTemperature, StartTemperature + TemperatureRange, Seq(
        InternalSimulationInitializer.getSimulation(StartTemperature, TemperatureRange, Mass, SpecificHeat),
        ExternalSimulationInitializer.getSimulation(StartTemperature, TemperatureRange, Mass, SpecificHeat)
      ))
      loader.setController(controller)
      val root: Parent = loader.load()
      new Scene(root, Width, Height)
      root
    }
    JavaFxToolkit.onFxThread {
      root
        .lookup("#simulatorSelectionComboBoxDelegate")
        .asInstanceOf[ComboBox[Simulation]]
        .getSelectionModel
        .select(shot.simulation)
      root
        .lookup("#processorSelectionComboBoxDelegate")
        .asInstanceOf[ComboBox[MainController.ComboSelections]]
        .setValue(shot.summary)
      root.lookup("#heatFlowSliderDelegate").asInstanceOf[Slider].setValue(shot.heatIn)
      root.lookup("#heatPerBeeSliderDelegate").asInstanceOf[Slider].setValue(shot.heatPerBee)
    }
    // Wait for the simulation to finish and the charts to update.
    Thread.sleep(3000)
    JavaFxToolkit.onFxThread {
      root.applyCss()
      root.layout()
      val image = root.snapshot(null, null)
      val buffered = new BufferedImage(image.getWidth.toInt, image.getHeight.toInt, BufferedImage.TYPE_INT_ARGB)
      val pixels = image.getPixelReader
      for (x <- 0 until buffered.getWidth; y <- 0 until buffered.getHeight) {
        buffered.setRGB(x, y, pixels.getArgb(x, y))
      }
      buffered
    }
  }
}
