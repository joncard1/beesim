package beesimulator.ui

import scalafx.scene.Node
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import java.net.URL
import java.{util => ju}
import java.io.IOException
import javafx.fxml.FXML
import javafx.scene.{
  control => jfxsc,
  layout => jfxsl,
  text => jfxst,
  chart => jfxch
}
import scalafx.scene.chart.LineChart
import beesimulator.ui.processors.BeeDataProcessor
import scalafx.beans.property._
import scalafx.scene.chart.XYChart
import scalafx.collections.ObservableBuffer
import math.BigDecimal.double2bigDecimal
import scalafx.scene.chart.NumberAxis
import javafx.collections.FXCollections

class BeeChartControl() extends Initializable {
  val loader = new FXMLLoader(getClass.getResource("beechart.fxml"))
  loader.setController(this)

  val simulatorData = ObjectProperty[Option[BeeDataset]](None)
  val processor = ObjectProperty[Option[BeeDataProcessor]](None)

  @FXML
  var xAxisDelegate: jfxch.NumberAxis = _
  lazy val xAxis: NumberAxis = new NumberAxis(xAxisDelegate)

  @FXML
  var chartDelegate: jfxch.LineChart[Number, Number] = _
  lazy val chart: LineChart[Number, Number] = new LineChart(chartDelegate)

  val minimumTemperature = DoubleProperty(0.0)
  val maximumTemperature = DoubleProperty(0.0)
  val numberOfBins = IntegerProperty(1)

  val view: LineChartWithIndicator = loader.load()

  override def initialize(x$0: URL, x$1: ju.ResourceBundle): Unit = {
    xAxis.lowerBound <== minimumTemperature
    xAxis.upperBound <== maximumTemperature
    chart.data() = FXCollections.observableArrayList()

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
    simulatorData.onChange((_, _, newData) =>
      regenerateCharts(
        minimumTemperature(),
        maximumTemperature(),
        numberOfBins(),
        processor(),
        newData
      )
      if(newData.isDefined) {
        view.indicatorPosition() = Option(newData.get.finalTemperature)
      }
    )
  }

  def regenerateCharts(
      minimumTemperature: Double,
      maximumTemperature: Double,
      numberOfBins: Integer,
      processor: Option[BeeDataProcessor],
      dataset: Option[BeeDataset]
  ) = {
    if (dataset.isDefined && processor.isDefined) {
      chart.data() = ObservableBuffer(
        XYChart.Series[Number, Number](
          dataset.get.name,
          ObservableBuffer.from(
            processor.get
              .transform(
                minimumTemperature,
                maximumTemperature,
                numberOfBins,
                dataset.get.bees
              )
              .zip(minimumTemperature to maximumTemperature by ((maximumTemperature - minimumTemperature)/numberOfBins))
              .map(tuple => {
                XYChart.Data(tuple._2, tuple._1)
              })
          )
        )
      )
    }
  }
}
