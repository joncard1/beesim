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
import scalafx.beans.property.DoubleProperty
import javafx.collections.ListChangeListener
import javafx.collections.ListChangeListener.Change
import scalafx.collections.ObservableBuffer
import javafx.collections.FXCollections
import java.util.ArrayList
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue

// TODO: Also, it should be easy to change the heat per bee at this point with a slider.

// This can't be a scalafx object because I need access to XYChart#getPlotChildren
class LineChartWithIndicator(
    @NamedArg("xAxis") xAxis: NumberAxis,
    @NamedArg("yAxis") yAxis: NumberAxis,
    @NamedArg("data") data: ObservableList[Series[Number, Number]] =
      new ObservableListWrapper(new ArrayList())
) extends jfxsc.LineChart[Number, Number](xAxis, yAxis, data) {
  val indicatorPosition = ObjectProperty[Option[Double]](None)

  private var indicatorLine: Option[Path] = None

  indicatorPosition.onChange((_, _, newFinalTemperature) => {
    updateIndicator(newFinalTemperature)
  })

  yAxis.scaleProperty().addListener(new ChangeListener[Number]() {
    def changed (
      observable: javafx.beans.value.ObservableValue[? <: Number],
      oldValue: Number,
      newValue: Number
    ): Unit = {
      updateIndicator(indicatorPosition())
    }
  })

  private def updateIndicator(newFinalTemperature: Option[Double]) = {
    if (newFinalTemperature.isDefined) {
      if (indicatorLine.isDefined) {
        this.getPlotChildren().remove(indicatorLine.get)
      }

      val startElement = new MoveTo(
        xAxis.getDisplayPosition(newFinalTemperature.get),
        yAxis.getDisplayPosition(0)
      )
      val lineElement = new LineTo(
        xAxis.getDisplayPosition(newFinalTemperature.get),
        yAxis.getDisplayPosition(yAxis.upperBoundProperty().get())
      )
      val path = new Path(startElement, lineElement)
      path.getStyleClass().setAll("temperature-indicator")
      indicatorLine = Some(path)
      this.getPlotChildren().add(indicatorLine.get)
    }

  }
}
