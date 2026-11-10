package beesimulator.ui

import scalafx.scene.chart.LineChart

import com.sun.javafx.collections.ObservableListWrapper
import javafx.scene.{chart => jfxsc}
import scalafx.beans.property.ObjectProperty
import javafx.collections.ObservableList
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart.Series
import javafx.beans.NamedArg
import javafx.scene.shape.Path
import javafx.scene.shape.PathElement
import scalafx.scene.shape.MoveTo.apply
import javafx.scene.shape.MoveTo
import javafx.scene.shape.LineTo
import javafx.scene.shape.Rectangle
import scalafx.beans.property.DoubleProperty
import javafx.collections.ListChangeListener
import javafx.collections.ListChangeListener.Change
import scalafx.collections.ObservableBuffer
import javafx.collections.FXCollections
import java.util.ArrayList

// TODO: Also, it should be easy to change the heat per bee at this point with a slider.

// This can't be a scalafx object because I need access to XYChart#getPlotChildren
class LineChartWithIndicator(
    @NamedArg("xAxis") xAxis: NumberAxis,
    @NamedArg("yAxis") yAxis: NumberAxis,
    @NamedArg("data") val data: ObservableList[Series[Number, Number]] = new ObservableListWrapper(new ArrayList())
) extends jfxsc.LineChart[Number, Number](xAxis, yAxis, data) {
  val indicatorPosition = ObjectProperty[Option[Double]](None)

  /**
    * The range of temperatures to shade behind the indicator, such as the
    * range a simulation that never settles kept swinging over at the end.
    */
  val indicatorBand = ObjectProperty[Option[(Double, Double)]](None)

  private var indicatorLine: Option[Path] = None

  /**
    * The shaded band, drawn behind the data in the indicator's colour.
    */
  private val band = new Rectangle()
  band.getStyleClass().setAll("temperature-band")
  band.setOpacity(0.25)
  band.setManaged(false)
  band.setVisible(false)
  this.getPlotChildren().add(0, band)

  indicatorPosition.onChange((_, _, newFinalTemperature) => {
    updateIndicator(newFinalTemperature)
  })

  indicatorBand.onChange((_, _, _) => positionIndicator())

  /**
    * Lays out the data, and then the indicator. The pixel position of the
    * indicator depends on the axes, which change whenever the chart is resized
    * or the data changes the range of the y-axis, so it is recalculated every
    * time the chart is laid out.
    */
  override protected def layoutPlotChildren(): Unit = {
    super.layoutPlotChildren()
    positionIndicator()
  }

  private def updateIndicator(newFinalTemperature: Option[Double]) = {
    indicatorLine.foreach(line => this.getPlotChildren().remove(line))
    indicatorLine = newFinalTemperature.map(_ => {
      val path = new Path(new MoveTo(), new LineTo())
      path.getStyleClass().setAll("temperature-indicator")
      this.getPlotChildren().add(path)
      band.fillProperty().bind(path.strokeProperty())
      path
    })
    positionIndicator()
  }

  private def positionIndicator(): Unit = {
    indicatorBand() match {
      case Some((low, high)) =>
        val left = xAxis.getDisplayPosition(low)
        val top = yAxis.getDisplayPosition(yAxis.getUpperBound())
        band.setX(left)
        band.setY(top)
        band.setWidth(math.max(xAxis.getDisplayPosition(high) - left, 0))
        band.setHeight(math.max(yAxis.getDisplayPosition(0) - top, 0))
        band.setVisible(true)
      case None =>
        band.setVisible(false)
    }
    for (line <- indicatorLine; temperature <- indicatorPosition()) {
      val x = xAxis.getDisplayPosition(temperature)
      val start = line.getElements().get(0).asInstanceOf[MoveTo]
      start.setX(x)
      start.setY(yAxis.getDisplayPosition(0))
      val end = line.getElements().get(1).asInstanceOf[LineTo]
      end.setX(x)
      end.setY(yAxis.getDisplayPosition(yAxis.getUpperBound()))
    }
  }
}
