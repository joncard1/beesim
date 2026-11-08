package beesimulator.ui

import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.shape.{MoveTo, Path, Rectangle}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import beesimulator.ui.processors._
import scala.jdk.CollectionConverters._

/**
  * Lays out the bee chart from beechart.fxml without showing it, and checks
  * where the indicator and the summary of the bee population are drawn.
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

    def show(processor: DataProcessor, thresholds: Seq[Double], indicator: Double, band: Option[(Double, Double)] = None): Unit = {
      controller.processor() = Some(processor)
      view.indicatorPosition() = Some(indicator)
      view.indicatorBand() = band
      controller.simulatorData() = Some(Dataset("test", indicator, 0, Seq(), thresholds.map(Item(_)), band))
      layout()
    }

    def indicatorX: Double = {
      val paths = view.lookupAll(".temperature-indicator").asScala.collect { case path: Path => path }
      paths.size shouldBe 1
      paths.head.getElements.get(0).asInstanceOf[MoveTo].getX
    }

    def expectedIndicatorX(temperature: Double): Double = controller.xAxis.getDisplayPosition(temperature)

    def band: Rectangle = view.lookup(".temperature-band").asInstanceOf[Rectangle]

    def points: Seq[(Double, Int)] = view.getData.get(0).getData.asScala.toSeq
      .map(point => (point.getXValue.doubleValue, point.getYValue.intValue))
  }

  val thresholds = (0 until 100).map(i => MinimumTemperature + i * 0.03)

  "The bee chart" when {
    "laid out with an indicator" should {
      "draw the indicator at the indicated temperature" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        JavaFxToolkit.onFxThread {
          val f = new Fixture(240, 200)
          f.show(HistogramDataProcessor, thresholds, 21.0)
          f.indicatorX shouldBe f.expectedIndicatorX(21.0) +- 0.5
        }
      }
    }

    "resized after the indicator is drawn" should {
      "move the indicator with the axis" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        JavaFxToolkit.onFxThread {
          val f = new Fixture(240, 200)
          f.show(HistogramDataProcessor, thresholds, 21.0)
          f.view.resize(480, 200)
          f.layout()
          f.indicatorX shouldBe f.expectedIndicatorX(21.0) +- 0.5
        }
      }
    }

    "switched from a histogram to a cumulative distribution" should {
      "keep the indicator at the indicated temperature" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        JavaFxToolkit.onFxThread {
          val f = new Fixture(240, 200)
          f.show(HistogramDataProcessor, thresholds, 21.0)
          f.show(CumulativeFrequencyDataProcessor, thresholds, 21.0)
          f.indicatorX shouldBe f.expectedIndicatorX(21.0) +- 0.5
        }
      }
    }

    "showing a cumulative distribution" should {
      "count bees only from the temperature of their thresholds" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        JavaFxToolkit.onFxThread {
          val f = new Fixture(240, 200)
          f.show(CumulativeFrequencyDataProcessor, Seq.fill(10)(20.03), 21.0)
          all(f.points.filter((x, _) => x < 20.03).map((_, count) => count)) shouldBe 0
          all(f.points.filter((x, _) => x > 20.03).map((_, count) => count)) shouldBe 10
          f.points.head._1 shouldBe 20.06 +- 1e-9
        }
      }
    }

    "showing a band around the indicator" should {
      "shade the band's range in the indicator's colour, following the axis when resized" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        JavaFxToolkit.onFxThread {
          val f = new Fixture(240, 200)
          f.show(HistogramDataProcessor, thresholds, 21.0, Some((20.8, 21.2)))
          for (width <- Seq(240.0, 480.0)) {
            f.view.resize(width, 200)
            f.layout()
            withClue(s"at width $width: ") {
              f.band.isVisible shouldBe true
              f.band.getX shouldBe f.expectedIndicatorX(20.8) +- 0.5
              (f.band.getX + f.band.getWidth) shouldBe f.expectedIndicatorX(21.2) +- 0.5
              f.band.getHeight should be > 0.0
            }
          }
          val indicator = f.view.lookup(".temperature-indicator").asInstanceOf[Path]
          f.band.getFill shouldBe indicator.getStroke
        }
      }

      "show no band when there is none" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        JavaFxToolkit.onFxThread {
          val f = new Fixture(240, 200)
          f.show(HistogramDataProcessor, thresholds, 21.0, Some((20.8, 21.2)))
          f.show(HistogramDataProcessor, thresholds, 21.0, None)
          f.band.isVisible shouldBe false
        }
      }
    }

    "showing a histogram" should {
      "plot each bin at its middle" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        JavaFxToolkit.onFxThread {
          val f = new Fixture(240, 200)
          f.show(HistogramDataProcessor, Seq.fill(10)(20.03), 21.0)
          f.points.head._1 shouldBe 20.03 +- 1e-9
          f.points.head._2 shouldBe 10
          f.points.size shouldBe NumberOfBins
        }
      }
    }
  }
}
