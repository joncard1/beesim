package beesimulator.ui

import javafx.collections.ObservableList
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.scene.chart.{LineChart, XYChart}
import javafx.scene.control.Slider
import javafx.scene.layout.Region
import javafx.scene.paint.Paint
import javafx.scene.shape.Path
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

    /**
      * The colour each series line is actually drawn in, in the order of the
      * series, after styles have been applied.
      */
    def lineColours(chart: javafx.scene.Node): Seq[Paint] = {
      chart.lookupAll(".chart-series-line").asScala.toSeq
        .map(_.asInstanceOf[Path])
        .sortBy(seriesIndex)
        .map(_.getStroke)
    }

    /**
      * The colour of each legend symbol, in the order of the series.
      */
    def legendColours(chart: javafx.scene.Node): Seq[Paint] = {
      chart.lookupAll(".chart-legend-item-symbol").asScala.toSeq
        .map(_.asInstanceOf[Region])
        .sortBy(seriesIndex)
        .map(_.getBackground.getFills.get(0).getFill)
    }

    private def seriesIndex(node: javafx.scene.Node): Int =
      node.getStyleClass.asScala.collectFirst { case c if c.matches("series\\d+") => c.drop(6).toInt }.get

    def styled[T](code: => T): T = JavaFxToolkit.onFxThread {
      root.applyCss()
      root.layout()
      code
    }
  }

  "The main view" when {
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

    "showing the data" should {
      "draw each legend symbol in the colour of its line" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        val f = new Fixture()
        f.awaitNewData(null)
        f.styled {
          val lines = f.lineColours(f.temperatureChart)
          lines.distinct.size shouldBe 8
          f.legendColours(f.temperatureChart) shouldBe lines
        }
      }

      "draw each bee chart's line and indicator in the colour of its temperature series" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        val f = new Fixture()
        f.awaitNewData(null)
        f.styled {
          val lines = f.lineColours(f.temperatureChart)
          for ((colour, i) <- lines.zipWithIndex) {
            val beeChart = f.root.lookup(s"#beechart-$i")
            withClue(s"bee chart $i: ") {
              f.lineColours(beeChart) shouldBe Seq(colour)
              beeChart.lookup(".temperature-indicator").asInstanceOf[Path].getStroke shouldBe colour
            }
          }
        }
      }
    }
  }
}
