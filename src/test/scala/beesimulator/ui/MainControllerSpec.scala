package beesimulator.ui

import javafx.collections.ObservableList
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.scene.chart.{LineChart, XYChart}
import javafx.scene.chart.NumberAxis
import javafx.scene.control.{ComboBox, Slider}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import beesimulator.controller.InternalSimulationInitializer
import scala.jdk.CollectionConverters._

/**
  * Loads the whole main view from beecharts.fxml with the internal simulation,
  * the way MainJFX does, and checks how the charts respond to the sliders.
  */
class MainControllerSpec extends AnyWordSpec with Matchers with Eventually {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(20, Seconds), interval = Span(50, Millis))

  type SeriesList = ObservableList[XYChart.Series[Number, Number]]

  class Fixture {
    val (root, temperatureChart, heatFlowSlider) = JavaFxToolkit.onFxThread {
      val loader = new FXMLLoader(classOf[MainController].getResource("beecharts.fxml"))
      val root: Parent = loader.load()
      val controller: MainController = loader.getController()
      controller.addSimulation(InternalSimulationInitializer.getSimulation(20.0, 3.0, 5, 0.5))
      new Scene(root, 1200, 800)
      controller.setData(19.8, 20.0, 23.0)
      (
        root,
        root.lookup("#temperatureChartDelegate").asInstanceOf[LineChart[Number, Number]],
        root.lookup("#heatFlowSliderDelegate").asInstanceOf[Slider]
      )
    }

    def seriesList: SeriesList = JavaFxToolkit.onFxThread(temperatureChart.getData)

    /**
      * Waits for the simulation to deliver results different from the given
      * ones.
      */
    def awaitNewData(previous: SeriesList): SeriesList = {
      eventually {
        val current = seriesList
        current should not be theSameInstanceAs(previous)
        current.size should be > 0
        current
      }
    }

    /**
      * The colour class of each series line, such as "series3", in the order
      * of the series.
      */
    def colourClasses: Seq[Seq[String]] = JavaFxToolkit.onFxThread {
      temperatureChart.getData.asScala.toSeq.map(series =>
        Option(series.getNode).toSeq.flatMap(_.getStyleClass.asScala.filter(_.matches("series\\d+")))
      )
    }
  }

  "The main view" when {
    "showing the bee charts" should {
      "give them all the same y-axis, covering every chart's data" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        val f = new Fixture()
        f.awaitNewData(null)
        for (summary <- Seq(MainController.ComboSelections.Cumulative, MainController.ComboSelections.Histogram)) {
          JavaFxToolkit.onFxThread {
            f.root.lookup("#processorSelectionComboBoxDelegate")
              .asInstanceOf[ComboBox[MainController.ComboSelections]].setValue(summary)
            f.root.applyCss()
            f.root.layout()
            val charts = (0 until 8).map(i => f.root.lookup(s"#beechart-$i").asInstanceOf[LineChart[Number, Number]])
            val bounds = charts.map(_.getYAxis.asInstanceOf[NumberAxis].getUpperBound)
            val largest = charts.flatMap(_.getData.get(0).getData.asScala.map(_.getYValue.doubleValue)).max
            withClue(s"$summary: ") {
              bounds.distinct should have size 1
              bounds.head should be >= largest
              bounds.head should be < 2 * largest
            }
          }
        }
      }
    }

    "the heat slider is moved" should {
      "keep each temperature series the same colour as its bee chart" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        val f = new Fixture()
        var data = f.awaitNewData(null)
        for (heat <- Seq(1.0, 2.0, 0.5)) {
          JavaFxToolkit.onFxThread(f.heatFlowSlider.setValue(heat))
          data = f.awaitNewData(data)
          withClue(s"after moving the heat slider to $heat: ") {
            f.colourClasses shouldBe data.asScala.indices.map(i => Seq(s"series$i"))
          }
        }
      }
    }
  }
}
