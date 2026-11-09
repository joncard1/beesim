package beesimulator.ui

import javafx.scene.Scene
import javafx.scene.chart.{NumberAxis, XYChart}
import javafx.scene.shape.{Path, Rectangle}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

/**
  * Lays out a chart with an indicator and a band, without showing it, and
  * checks where and how the band is drawn.
  */
class IndicatorBandSpec extends AnyWordSpec with Matchers {

  class Fixture {
    val xAxis = new NumberAxis(20, 23, 0.5)
    xAxis.setAutoRanging(false)
    val yAxis = new NumberAxis()
    val view = new LineChartWithIndicator(xAxis, yAxis)
    val series = new XYChart.Series[Number, Number]()
    (0 until 50).foreach(i => series.getData.add(new XYChart.Data[Number, Number](20 + i * 0.06, i)))
    view.getData.add(series)
    new Scene(view, 240, 200)

    def layout(): Unit = {
      view.applyCss()
      view.layout()
    }

    def band: Rectangle = view.lookup(".temperature-band").asInstanceOf[Rectangle]
  }

  "A chart with an indicator" when {
    "showing a band around the indicator" should {
      "shade the band's range in the indicator's colour, following the axis when resized" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        JavaFxToolkit.onFxThread {
          val f = new Fixture()
          f.view.indicatorPosition() = Some(21.0)
          f.view.indicatorBand() = Some((20.8, 21.2))
          for (width <- Seq(240.0, 480.0)) {
            f.view.resize(width, 200)
            f.layout()
            withClue(s"at width $width: ") {
              f.band.isVisible shouldBe true
              f.band.getX shouldBe f.xAxis.getDisplayPosition(20.8) +- 0.5
              (f.band.getX + f.band.getWidth) shouldBe f.xAxis.getDisplayPosition(21.2) +- 0.5
              f.band.getHeight should be > 0.0
            }
          }
          val indicator = f.view.lookup(".temperature-indicator").asInstanceOf[Path]
          f.band.getFill shouldBe indicator.getStroke
        }
      }
    }

    "the band is removed" should {
      "show no band" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        JavaFxToolkit.onFxThread {
          val f = new Fixture()
          f.view.indicatorPosition() = Some(21.0)
          f.view.indicatorBand() = Some((20.8, 21.2))
          f.layout()
          f.view.indicatorBand() = None
          f.layout()
          f.band.isVisible shouldBe false
        }
      }
    }
  }
}
