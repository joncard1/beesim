package beesimulator.ui

import javafx.fxml.Initializable
import java.net.URL
import java.{util => ju}
import javafx.fxml.FXML
import javafx.scene.{
  control => jfxsc,
  layout => jfxsl,
  text => jfxst,
  chart => jfxch
}
import javafx.{event => jfxe, fxml => jfxf, scene => jfxs}
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.chart._
import scalafx.scene.text._
import javafx.beans.NamedArg
import scalafx.beans.property._
import scalafx.beans._
import scalafx.collections._
import javafx.beans.property.ListProperty
import scalafx.beans.value.ObservableValue._
import scalafx.beans.binding.Bindings
import javafx.collections.ObservableList
import javafx.collections.FXCollections
import java.util.ArrayList
import javafx.fxml.FXMLLoader
import java.io.IOException
import beesimulator.ui.processors.BeeDataProcessor
import beesimulator.ui.processors.HistogramBeeDataProcessor
import beesimulator.ui.processors.CumulativeCountBeeDataProcessor

/** This was to try to use fx:constant on <Test.ComboSelections> in
  * beecharts.fxml. I could only get it to recognize the type if Test was an
  * object and ComboSelections was either an enum or a trait or a class inside
  * it. But enums are implemented as a sealed class and the value are values
  * within the companion object, meaning that ComboSelections.Histogram is not a
  * value in a type, but a value in a value, and fx:constant doesn't go for
  * that. The values are not named "Histogram" and "Cumulative", though; they
  * are named "Histogram$lzy1" and "Cumulative$lzy1" and for some reason JavaFX
  * does not believe they are declared constants. the value constant.
  */
object MainController {
  enum ComboSelections(value: String) {
    case Histogram extends ComboSelections("Histogram")
    case Cumulative extends ComboSelections("Cumulative")
  }
}

class MainController extends Initializable {
  val NumberOfBins = 50
  val InitialHeatFlow = 0.5

  @FXML var processorSelectionComboBoxDelegate: jfxsc.ComboBox[MainController.ComboSelections] = _
  private lazy val processorSelectionComboBox: ComboBox[MainController.ComboSelections] = new ComboBox(
    processorSelectionComboBoxDelegate
  )

  @FXML
  private var borderDelegate: jfxsl.BorderPane = _
  private lazy val borderPane: BorderPane = new BorderPane(borderDelegate)

  @FXML
  private var heatFlowTextDelegate: jfxst.Text = _
  private lazy val heatFlowText: Text = new Text(heatFlowTextDelegate)

  @FXML
  private var heatFlowSliderDelegate: jfxsc.Slider = _
  private lazy val heatFlowSlider: Slider = new Slider(heatFlowSliderDelegate)

  @FXML
  private var temperatureChartDelegate: jfxch.LineChart[Number, Number] = _
  private lazy val temperatureChart: LineChart[Number, Number] = new LineChart(
    temperatureChartDelegate
  )

  @FXML
  private var beeChartsBoxDelegate: jfxsl.HBox = _
  private lazy val beeChartsBox: HBox = new HBox(beeChartsBoxDelegate)

  @FXML
  private var yAxisDelegate: jfxch.NumberAxis = _
  private lazy val yAxis: NumberAxis = new NumberAxis(yAxisDelegate)

  @FXML var xAxisDelegate: jfxch.NumberAxis = _
  private lazy val xAxis: NumberAxis = new NumberAxis(xAxisDelegate)

  val minimumYValue = new DoubleProperty()
  val minimumYBoundary = new DoubleProperty()
  val maximumYBoundary = new DoubleProperty()

  val simulatorData: BufferProperty[BeeDataset] = new BufferProperty(
    ObservableBuffer()
  )

  var beeCharts = Array[BeeChartControl]()
  val processor = ObjectProperty[Option[BeeDataProcessor]](None)

  override def initialize(x$0: URL, x$1: ju.ResourceBundle): Unit = {
    heatFlowText.text <== heatFlowSlider.value.asString("%#1.2f")
    yAxis.lowerBound <== minimumYValue
    yAxis.upperBound <== maximumYBoundary
    processorSelectionComboBox.value.onChange((_, _, newValue) => {
      newValue match {
        case MainController.ComboSelections.Histogram => processor() = Some(HistogramBeeDataProcessor)
        case MainController.ComboSelections.Cumulative => processor() = Some(CumulativeCountBeeDataProcessor)
      }
    })
    processorSelectionComboBox.value() = MainController.ComboSelections.Histogram
  }

  def setData(
      newMinimumYValue: Double,
      newMinimumYBoundary: Double,
      newMaximumYBoundary: Double,
      generator: DataGenerator
  ) = {
    minimumYValue() = newMinimumYValue
    minimumYBoundary() = newMinimumYBoundary
    maximumYBoundary() = newMaximumYBoundary
    beeCharts = new Array[BeeChartControl](generator.numberOfFamilies)
    for (i <- 0 until generator.numberOfFamilies) {
      val newBeeChartController = new BeeChartControl()
      newBeeChartController.processor <== processor
      newBeeChartController.minimumTemperature <== minimumYBoundary
      newBeeChartController.maximumTemperature <== maximumYBoundary
      newBeeChartController.numberOfBins() = NumberOfBins

      simulatorData.onChange((_, _, newDatasets) => {
        if (i < newDatasets.size()) { // Because I clear it first, it gets run once with no data and that causes an index out of bounds exception.
          newBeeChartController.simulatorData() = Option(newDatasets.get(i))
        }
      })

      val view = newBeeChartController.view
      view.idProperty.set(s"beechart-${i}")
      beeChartsBox.children.add(view)
      beeCharts(i) = (newBeeChartController)
    }

    heatFlowSlider.value.onChange((_, _, newValue) => {
      simulatorData().clear()
      simulatorData().addAll(
        generator.generatePoints(newValue.doubleValue())
      )
    })

    import scalafx.collections.ObservableBuffer._
    import scalafx.scene.chart.XYChart.Series._
    val subscription = simulatorData().onChange((newValue, _) => {
      temperatureChart.data() = simulatorData()
        .map[javafx.scene.chart.XYChart.Series[Number, Number]](dataset => {
          XYChart.Series[Number, Number](
            dataset.name,
            ObservableBuffer.from(
              dataset.temperatures.zipWithIndex.map(tuple =>
                XYChart.Data(tuple._2, tuple._1)
              )
            )
          )
        })
    })
    heatFlowSlider.value() = InitialHeatFlow
    heatFlowSlider.requestFocus()
  }

}
