package beesimulator.ui

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.shape.{MoveTo, Path}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import beesimulator.ui.processors._
import scala.jdk.CollectionConverters._

/**
  * Lays out the bee chart from beechart.fxml without showing it, and checks
  * where the indicator and the summary of the bee population are drawn.
  * Skipped when there is no display for JavaFX to start on.
  */
class BeeChartSpec extends AnyWordSpec with Matchers {

  final case class Item(v: Double) extends HistogramItem {
    def value(): Double = v
  }

  val MinimumTemperature = 20.0
  val MaximumTemperature = 23.0
  val NumberOfBins = 50

  /**
    * A chart loaded from FXML and configured the way MainController configures
    * it, laid out at the given size.
    */
  class Fixture(width: Double, height: Double) {
    val loader = new FXMLLoader(classOf[BeeChartControl].getResource("beechart.fxml"))
    val view: LineChartWithIndicator = loader.load()
    val controller: BeeChartControl = loader.getController()
    controller.minimumTemperature() = MinimumTemperature
    controller.maximumTemperature() = MaximumTemperature
    controller.numberOfBins() = NumberOfBins
    val scene = new Scene(view, width, height)
    layout()

    def layout(): Unit = {
      view.applyCss()
      view.layout()
    }

    def show(processor: DataProcessor, thresholds: Seq[Double], indicator: Double): Unit = {
      controller.processor() = Some(processor)
      view.indicatorPosition() = Some(indicator)
      controller.simulatorData() = Some(Dataset("test", indicator, 0, Seq(), thresholds.map(Item(_))))
      layout()
    }

    def indicatorX: Double = {
      val paths = view.lookupAll(".temperature-indicator").asScala.collect { case path: Path => path }
      paths.size shouldBe 1
      paths.head.getElements.get(0).asInstanceOf[MoveTo].getX
    }

    def expectedIndicatorX(temperature: Double): Double = controller.xAxis.getDisplayPosition(temperature)

    def points: Seq[(Double, Int)] = view.getData.get(0).getData.asScala.toSeq
      .map(point => (point.getXValue.doubleValue, point.getYValue.intValue))
  }

  val thresholds = (0 until 100).map(i => MinimumTemperature + i * 0.03)

  "The bee chart" when {
    "laid out with an indicator" should {
      "draw the indicator at the indicated temperature" in {
        assume(JavaFxToolkit.available, "JavaFX could not start")
        JavaFxToolkit.onFxThread {
          val f = new Fixture(240, 200)
          f.show(HistogramDataProcessor, thresholds, 21.0)
          f.indicatorX shouldBe f.expectedIndicatorX(21.0) +- 0.5
        }
      }
    }

    // Known issue: the indicator is only redrawn when the indicator or the
    // y-axis scale changes, so when the chart gets wider or narrower the
    // indicator stays at its old pixel position.
    "resized after the indicator is drawn" should {
      "move the indicator with the axis" in {
        assume(JavaFxToolkit.available, "JavaFX could not start")
        JavaFxToolkit.onFxThread {
          val f = new Fixture(240, 200)
          f.show(HistogramDataProcessor, thresholds, 21.0)
          f.view.resize(480, 200)
          f.layout()
          pendingUntilFixed {
            f.indicatorX shouldBe f.expectedIndicatorX(21.0) +- 0.5
          }
        }
      }
    }

    "switched from a histogram to a cumulative distribution" should {
      "keep the indicator at the indicated temperature" in {
        assume(JavaFxToolkit.available, "JavaFX could not start")
        JavaFxToolkit.onFxThread {
          val f = new Fixture(240, 200)
          f.show(HistogramDataProcessor, thresholds, 21.0)
          f.show(CumulativeFrequencyDataProcessor, thresholds, 21.0)
          f.indicatorX shouldBe f.expectedIndicatorX(21.0) +- 0.5
        }
      }
    }

    // Known issue: each bin is plotted at its lower edge, but the cumulative
    // count of a bin includes bees up to its upper edge, so the curve is drawn
    // one bin width (0.06 degrees) to the left of where it should be. Bees at
    // 20.03 are shown as already counted at 20.0.
    "showing a cumulative distribution" should {
      "not count bees before the temperature of their thresholds" in {
        assume(JavaFxToolkit.available, "JavaFX could not start")
        JavaFxToolkit.onFxThread {
          val f = new Fixture(240, 200)
          f.show(CumulativeFrequencyDataProcessor, Seq.fill(10)(20.03), 21.0)
          pendingUntilFixed {
            all(f.points.filter((x, _) => x < 20.03).map((_, count) => count)) shouldBe 0
          }
        }
      }
    }
  }
}
