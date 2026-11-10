package beesimulator.ui

import javafx.fxml.FXMLLoader
import javafx.scene.{Node, Parent, Scene}
import javafx.scene.chart.LineChart
import javafx.scene.control.{ComboBox, Slider}
import javafx.scene.input.MouseButton
import javafx.stage.Stage
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.testfx.api.FxRobot
import org.testfx.util.WaitForAsyncUtils
import beesimulator.controller.{ExternalSimulationInitializer, InternalSimulationInitializer}
import scala.jdk.CollectionConverters._

/**
  * Drives the main window the way a user does, with TestFX's robot: dragging
  * the sliders and choosing simulations from the menu.
  */
class InteractionSpec extends AnyWordSpec with Matchers with Eventually {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(20, Seconds), interval = Span(100, Millis))

  class Fixture {
    val internal = InternalSimulationInitializer.getSimulation(20.0, 3.0, 5, 0.5)
    val external = ExternalSimulationInitializer.getSimulation(20.0, 3.0, 5, 0.5)

    val (stage, root) = JavaFxToolkit.onFxThread {
      val loader = new FXMLLoader(classOf[MainController].getResource("beecharts.fxml"))
      val root: Parent = loader.load()
      val controller: MainController = loader.getController()
      controller.addSimulation(internal)
      controller.addSimulation(external)
      val stage = new Stage()
      stage.setScene(new Scene(root, 1200, 800))
      controller.setData(19.8, 20.0, 23.0)
      stage.show()
      (stage, root)
    }
    WaitForAsyncUtils.waitForFxEvents()

    val robot = new FxRobot()

    def lookup[T](selector: String): T = JavaFxToolkit.onFxThread(root.lookup(selector).asInstanceOf[T])
    val heatFlowSlider: Slider = lookup("#heatFlowSliderDelegate")
    val heatPerBeeSlider: Slider = lookup("#heatPerBeeSliderDelegate")
    val temperatureChart: LineChart[Number, Number] = lookup("#temperatureChartDelegate")
    val simulationMenu: ComboBox[Simulation] = lookup("#simulatorSelectionComboBoxDelegate")

    /**
      * The last temperature of each series in the temperature chart.
      */
    def finalTemperatures: Seq[Double] = JavaFxToolkit.onFxThread {
      temperatureChart.getData.asScala.toSeq.map(series => series.getData.asScala.last.getYValue.doubleValue)
    }

    def close(): Unit = JavaFxToolkit.onFxThread(stage.close())
  }

  "The main window" when {
    "the heat slider is dragged" should {
      "end up showing the simulation for where the slider was released" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        val f = new Fixture()
        try {
          eventually(f.finalTemperatures should have size 8)
          val startValue = JavaFxToolkit.onFxThread(f.heatFlowSlider.getValue)
          val thumb: Node = JavaFxToolkit.onFxThread(f.heatFlowSlider.lookup(".thumb"))
          // Drag upwards in small steps, as a user would, which sends many
          // changes while simulations are still running.
          f.robot.drag(thumb, MouseButton.PRIMARY)
          for (_ <- 1 to 10) f.robot.moveBy(0, -15)
          f.robot.drop()
          WaitForAsyncUtils.waitForFxEvents()

          val (heatIn, heatPerBee) = JavaFxToolkit.onFxThread((f.heatFlowSlider.getValue, f.heatPerBeeSlider.getValue))
          heatIn should be > startValue
          val expected = f.internal.generator.generatePoints(heatIn, heatPerBee).map(_.temperatures.last)
          eventually(f.finalTemperatures shouldBe expected)
        } finally f.close()
      }
    }

    "switching between simulations" should {
      "fill the charts straight away, without moving a slider" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        val f = new Fixture()
        try {
          eventually(f.finalTemperatures should have size 8)
          for (
            (name, series) <- Seq(
              f.external.simulatorName -> 3,
              f.internal.simulatorName -> 8,
              f.external.simulatorName -> 3
            )
          ) {
            f.robot.clickOn(f.simulationMenu)
            f.robot.clickOn(name)
            WaitForAsyncUtils.waitForFxEvents()
            withClue(s"after choosing $name: ") {
              eventually {
                JavaFxToolkit.onFxThread(f.simulationMenu.getValue.simulatorName) shouldBe name
                f.finalTemperatures should have size series
                val beeCharts = (0 until series).map(i => f.lookup[LineChart[Number, Number]](s"#beechart-$i"))
                all(JavaFxToolkit.onFxThread(beeCharts.map(_.getData.size))) shouldBe 1
              }
            }
          }
        } finally f.close()
      }
    }
  }
}
