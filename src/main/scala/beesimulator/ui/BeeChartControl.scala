package beesimulator.ui

import scalafx.scene.Node
import javafx.fxml.Initializable
import java.net.URL
import java.{util => ju}
import java.io.IOException
import javafx.fxml.FXML
import javafx.scene.{control => jfxsc, layout => jfxsl, text => jfxst, chart => jfxch}
import scalafx.scene.chart.LineChart
import beesimulator.ui.processors.DataProcessor
import scalafx.beans.property._
import scalafx.scene.chart.XYChart
import scalafx.collections.ObservableBuffer
import scalafx.scene.chart.NumberAxis
import javafx.collections.FXCollections
import scalafx.Includes.jfxObjectProperty2sfx
import scala.compiletime.uninitialized

/**
  * Controller class for a chart that summarizes a population in a [[beesimulator.ui.Dataset]] using a provided [[beesimulator.ui.processors.DataProcessor]]. Create an instance of this class, and then the control itself can be accessed in the property [[view]].
  */
class BeeChartControl() extends Initializable {

  /**
    * The simulation data to be summarized by this control.
    */
  val simulatorData = ObjectProperty[Option[Dataset]](None)

  /**
    * The processor used to summarize the data.
    */
  val processor = ObjectProperty[Option[DataProcessor]](None)

  /**
    * The X-axis of the chart contained in this control.
    */
  @FXML
  var xAxisDelegate: jfxch.NumberAxis = uninitialized

  /**
    * The Scala version of the X-axis of the chart container in this control.
    */
  lazy val xAxis: NumberAxis = new NumberAxis(xAxisDelegate)

  @FXML
  var yAxisDelegate: jfxch.NumberAxis = uninitialized
  lazy val yAxis: NumberAxis = new NumberAxis(yAxisDelegate)

  /**
    * The largest value in the summary this chart is currently showing. The
    * charts summarizing the different populations watch each other's maximums
    * to share a y-axis, so that they can be compared.
    */
  val maximumValue = DoubleProperty(0.0)

  /**
    * The largest value the y-axis has to show, shared by all of the bee charts.
    * When it is 0, the chart chooses its own y-axis range.
    */
  val sharedMaximum = DoubleProperty(0.0)

  /**
    * The minimum temperature to display. This is the lowest value on the X-axis.
    */
  val minimumTemperature = DoubleProperty(0.0)

  /**
    * The maximum temperature to display. This is the highest value on the X-axis.
    */
  val maximumTemperature = DoubleProperty(0.0)

  /**
    * The number of bins used when calculating the histograms.
    */
  val numberOfBins = IntegerProperty(1)

  @FXML
  var chartDelegate: jfxch.LineChart[Number, Number] = uninitialized
  lazy val chart: LineChart[Number, Number] = new LineChart(chartDelegate)

  /**
    * Initializes the change listeners.
    *
    * @param url        URL of the JFX control.
    * @param resources  Resource bundle to use rendering the control.
    */
  override def initialize(url: URL, resources: ju.ResourceBundle): Unit = {
    xAxis.lowerBound <== minimumTemperature
    xAxis.upperBound <== maximumTemperature
    chart.data() = FXCollections.observableArrayList()
    sharedMaximum.onChange((_, _, newMaximum) => scaleYAxis(newMaximum.doubleValue()))

    minimumTemperature.onChange((_, _, newTemperature) =>
      regenerateCharts(
        newTemperature.doubleValue(),
        maximumTemperature(),
        numberOfBins(),
        processor(),
        simulatorData()
      )
    )
    maximumTemperature.onChange((_, _, newTemperature) =>
      regenerateCharts(
        minimumTemperature(),
        newTemperature.doubleValue(),
        numberOfBins(),
        processor(),
        simulatorData()
      )
    )
    numberOfBins.onChange((_, _, newBins) =>
      regenerateCharts(
        minimumTemperature(),
        maximumTemperature(),
        newBins.intValue(),
        processor(),
        simulatorData()
      )
    )
    processor.onChange((_, _, newProcessor) =>
      regenerateCharts(
        minimumTemperature(),
        maximumTemperature(),
        numberOfBins(),
        newProcessor,
        simulatorData()
      )
    )
    simulatorData.onChange((_, _, newData) => {
      chart.title = newData.map(_.name).getOrElse("")
      regenerateCharts(
        minimumTemperature(),
        maximumTemperature(),
        numberOfBins(),
        processor(),
        newData
      )
    })
  }

  /**
    * Regenerates the charts in response to changes in a variety of properties.
    *
    * @param minimumTemperature The minimum value of the X-axis.
    * @param maximumTemperature The maximum value of the Y-axis.
    * @param numberOfBins       The number of bins to use to calculate the histogram.
    * @param processor          The processor to use to calculate the histogram.
    * @param dataset            The data set to summarize in the histogram.
    */
  private def regenerateCharts(
      minimumTemperature: Double,
      maximumTemperature: Double,
      numberOfBins: Integer,
      processor: Option[DataProcessor],
      dataset: Option[Dataset]
  ) = {
    if (dataset.isDefined && processor.isDefined) {
      val binWidth = (maximumTemperature - minimumTemperature) / numberOfBins
      val values = processor.get.transform(
        minimumTemperature,
        maximumTemperature,
        numberOfBins,
        dataset.get.bees
      )
      chart.data() = ObservableBuffer(
        XYChart.Series[Number, Number](
          dataset.get.name,
          ObservableBuffer.from(
            values.zipWithIndex
              .map((value, bin) => {
                XYChart.Data[Number, Number](processor.get.binPosition(minimumTemperature, binWidth, bin), value)
              })
          )
        )
      )
      maximumValue() = values.maxOption.getOrElse(0).toDouble
    }
  }

  /**
    * Fixes the y-axis to show from 0 to at least the given maximum, or lets it
    * range automatically if the maximum is 0.
    *
    * @param maximum The largest value the y-axis has to show.
    */
  private def scaleYAxis(maximum: Double): Unit = {
    if (maximum > 0) {
      val (upperBound, tickUnit) = AxisScale.forMaximum(maximum)
      yAxis.autoRanging = false
      yAxis.lowerBound = 0
      yAxis.upperBound = upperBound
      yAxis.tickUnit = tickUnit
    } else {
      yAxis.autoRanging = true
    }
  }
}
