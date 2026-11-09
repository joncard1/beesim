package beesimulator.ui

import javafx.fxml.FXMLLoader
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
import javafx.{scene => jfxs}
import scalafx.scene.layout._
import scalafx.scene.control._
import scalafx.scene.chart._
import scalafx.scene.text._
import scalafx.beans.property._
import scalafx.collections._
import beesimulator.ui.processors._
import scalafx.application.Platform
import scalafx.event.subscriptions.Subscription
import scalafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import java.util.ArrayList
import scalafx.util.StringConverter
import scala.compiletime.uninitialized

// TODO: It occurs to me that this needs to be much more defensive if I'm
// expecting people to potentially write their own bees or generators.

/**
 * Defines the choices available to summarize the population of bees. The
 * possible values are limited, so I find it better to use a value type that is
 * limited, unlike a simple string, which can theoretically have any value that
 * a string can be. In mathematical terms, this is the set with no operations
 * defined on it, and if I intend the set to be limited, it doesn't make much
 * sense to use a set of infinite size, like strings, to represent it.
 */
object MainController {
  enum ComboSelections(value: String) {
    case Histogram extends ComboSelections("Histogram")
    case Cumulative extends ComboSelections("Cumulative")
  }
}

/**
 * The controller class of the principal view of the application constructed
 * from beecharts.fxml. We can keep the behavior here, while keeping the
 * instructions on how it is laid out and displayed there, which makes making
 * cosmetic changes much easier. I was a big fan of Adobe Flex back in the day;
 * I'm sorry I didn't learn JavaFX sooner!
 *
 * Note about implementation: because this was created in Scala, a language that
 * was created to compile into code that executes in the Java runtime, we can
 * use a library like JavaFX, which is used to implement all of the windows and
 * controls for our GUI. However, the system that loads the XML file was written
 * in Java, and it assumes that it is supposed to create objects that were
 * written in Java. The library we use that wraps around the JavaFX classes,
 * ScalaFX, lets us express ourselves in Scala. But here, we have to give the
 * JavaFX loader variables that we call "delegates" to put the Java classes in.
 * Then, we wrap those in the ScalaFX classes. Because we define the ScalaFX
 * classes as "lazy", they aren't instantiated until something uses them, and by
 * that time the JavaFX system has populated the delegates.
 */
class MainController {

  /**
   * The simple, default way to launching background threads.
   */
  private val backgroundThreads = scala.concurrent.ExecutionContext.global

  /**
   * The number of bins used when generating the histograms that summarize the
   * bee populations.
   */
  val NumberOfBins = 50

  /**
   * The initial type of processor to use to summarize the bee populations.
   */
  val InitialProcessorSelection = MainController.ComboSelections.Histogram

  @FXML var borderPaneDelegate: jfxs.layout.BorderPane = uninitialized
  private lazy val borderPane = new BorderPane(borderPaneDelegate)

  @FXML var simulatorSelectionComboBoxDelegate: jfxsc.ComboBox[Simulation] = uninitialized
  private lazy val simulatorSelectionComboBox: ComboBox[Simulation] = new ComboBox(simulatorSelectionComboBoxDelegate)
  /**
   * The combo box that allows users to select the style of summary used to
   * summarize the bee populations in the charts across the top. It uses the
   * values in the enum [[beesimulator.ui.MainController.ComboSelections]] up above.
   */
  @FXML var processorSelectionComboBoxDelegate: jfxsc.ComboBox[MainController.ComboSelections] = uninitialized
  private lazy val processorSelectionComboBox: ComboBox[MainController.ComboSelections] = new ComboBox(
    processorSelectionComboBoxDelegate
  )

  /**
   * The text box that displays the amount of heat the slider
   * [[heatFlowSlider]] has been set below.
   */
  @FXML
  private var heatFlowTextDelegate: jfxst.Text = uninitialized
  private lazy val heatFlowText: Text = new Text(heatFlowTextDelegate)

  @FXML
  private var heatPerBeeTextDelegate: jfxst.Text = uninitialized
  private lazy val heatPerBeeText: Text = new Text(heatPerBeeTextDelegate)

  /**
   * The slider that sets the amount of heat flow into the hive.
   */
  @FXML
  private var heatFlowSliderDelegate: jfxsc.Slider = uninitialized
  private lazy val heatFlowSlider: Slider = new Slider(heatFlowSliderDelegate)

  @FXML
  private var heatPerBeeSliderDelegate: jfxsc.Slider = uninitialized
  private lazy val heatPerBeeSlider: Slider = new Slider(heatPerBeeSliderDelegate)

  /**
   * The chart used to display the temperature data over time as a result of
   * both the heat flow into the hive and the bees expelling heat.
   */
  @FXML
  private var temperatureChartDelegate: jfxch.LineChart[Number, Number] = uninitialized
  private lazy val temperatureChart: LineChart[Number, Number] = new LineChart(
    temperatureChartDelegate
  )

  /**
   * The box that contains the charts summarizing the bee populations. The
   * charts are dynamically created, one for each family in the data generator,
   * so this cannot be defined in beecharts.fxml. We can separate the display
   * logic and the behavior, though, by defining the template for each chart in
   * beechart.fxml.
   */
  @FXML
  private var beeChartsBoxDelegate: jfxsl.HBox = uninitialized
  private lazy val beeChartsBox: HBox = new HBox(beeChartsBoxDelegate)

  /**
   * The yAxis of the temperature chart.
   */
  @FXML
  private var yAxisDelegate: jfxch.NumberAxis = uninitialized
  private lazy val yAxis: NumberAxis = new NumberAxis(yAxisDelegate)

  /**
   * Shows why the charts stopped updating when a simulation run fails, and is
   * hidden otherwise.
   */
  @FXML
  private var errorLabelDelegate: jfxsc.Label = uninitialized
  private lazy val errorLabel: Label = new Label(errorLabelDelegate)

  /**
   * Shows an error message below the charts, or hides it when there is none.
   *
   * @param message The message to show, or None to hide it.
   */
  private def showError(message: Option[String]): Unit = {
    errorLabel.text = message.getOrElse("")
    errorLabel.visible = message.isDefined
    errorLabel.managed = message.isDefined
  }

  /**
   * The minimum Y value to display on the temperature chart.
   */
  val minimumYValue = new DoubleProperty()

  /**
   * The minimum of the range of thresholds that were generated. This is used to
   * calibrate the charts that summarize the bee populations, and also, in a
   * previous version, had an indicator on the temperature chart, but that
   * wasn't implemented here.
   */
  val minimumYBoundary = new DoubleProperty()

  /**
   * The maximum of the range of thresholds that were generated. This is used to
   * calibrate the charts that summarize the bee populations, and also, in a
   * previous version, had an indicator on the temperature chart, but that
   * wasn't implemented here.
   */
  val maximumYBoundary = new DoubleProperty()

  /**
   * The data from the simulator. When the data is refreshed, this object issues
   * a change event, so elements of the GUI can subscribe to this object and be
   * updated automatically.
   */
  val simulatorData: BufferProperty[Dataset] = new BufferProperty(
    ObservableBuffer()
  )

  /**
   * The controllers for the charts that summarize the bee populations. The the
   * GUI element is added to the root node, and then not interacted with
   * directly. The objects in this array control the behavior of those elements,
   * and are specific to this application.
   */
  var beeCharts = Array[BeeChartControl]()

  /**
    * The largest value shown by any of the bee charts, so that they can all
    * use the same y-axis and be compared with each other.
    */
  val beeChartMaximum = new DoubleProperty()

  /**
   * The processor that gets substituted in the middle of the reactive chain
   * between the simulator data generators and the charts that summarize the bee
   * populations. I might wish that this could be a function type, but
   * using the ObjectProperty, the property can subscribe to [[simulatorData]]
   * and be subscribed to by the charts, and assigning an object on the event of
   * changing the [[processorSelectionComboBoxDelegate]] triggers the entire
   * processing pipeline without necessarily re-running the simulator.
   */
  val processor = ObjectProperty[Option[DataProcessor]](None)

  /**
   * Runs the currently selected simulation in the background as the sliders
   * move. It is replaced when a different simulation is selected.
   */
  private var simulationRunner: Option[ConflatingRunner[(Double, Double), List[Dataset]]] = None

  /**
    *
    * @param newSimulation
    * @return
    */
  // If this is to be called after setData, this needs to be more complex.
  def addSimulation(newSimulation: Simulation) = {
    simulatorSelectionComboBox.items().add(newSimulation)
  }

  def setData(
      newMinimumYValue: Double,
      newMinimumYBoundary: Double,
      newMaximumYBoundary: Double
  ) = {
    // Inialize the various properties with either a default value or the provided values.
    temperatureChart.data() = FXCollections.observableArrayList()
    minimumYValue() = newMinimumYValue
    minimumYBoundary() = newMinimumYBoundary
    maximumYBoundary() = newMaximumYBoundary

    // Initialize the simple bindings
    heatFlowText.text <== heatFlowSlider.value.asString("%#1.2f")
    heatPerBeeText.text <== heatPerBeeSlider.value.asString("%#1.4f")
    yAxis.lowerBound <== minimumYValue
    yAxis.upperBound <== maximumYBoundary

    // Bind the state of the processor to the combo box and initialize it
    processorSelectionComboBox.value.onChange((_, _, newValue) => {
      newValue match {
        case MainController.ComboSelections.Histogram => processor() = Some(HistogramDataProcessor)
        case MainController.ComboSelections.Cumulative => processor() = Some(CumulativeFrequencyDataProcessor)
      }
    })
    processorSelectionComboBox.value() = InitialProcessorSelection

    // Subscribes the temperature chart to the simulatorData
    simulatorData.onChange((_, _, newValue) => {
      temperatureChart.data() = simulatorData()
        .map[jfxch.XYChart.Series[Number, Number]](dataset => {
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

    simulatorSelectionComboBox.setConverter(new StringConverter[Simulation] {

      override def fromString(string: String): Simulation = {
        val simulations = simulatorSelectionComboBox.items().filtered(sim => sim.simulatorName.equals(string))
        if (simulations.size() > 0) {
          simulations.get(0)
        } else {
          throw new Exception("No valid simulation found")
        }
      }

      override def toString(t: Simulation): String = {
        if (t != null) {
          t.simulatorName
        } else {
          ""
        }
      }
    })

    var simulatorDataSubscriptions: Option[Array[Subscription]] = None
    var heatFlowSliderSubscription: Option[Subscription] = None
    var heatPerBeeSliderSubscription: Option[Subscription] = None
    simulatorSelectionComboBox.value.onChange((_, _, newSimulation) => {
      // Disconnect that previous simulation from the UI elements.
      if (simulatorDataSubscriptions.isDefined) {
        simulatorDataSubscriptions.get.foreach(sub => sub.cancel())
      }
      if (heatFlowSliderSubscription.isDefined) {
        heatFlowSliderSubscription.get.cancel()
      }
      if (heatPerBeeSliderSubscription.isDefined) {
        heatPerBeeSliderSubscription.get.cancel()
      }

      // For each of the families in the generator, creat a chart to summarize the
      // bee population and subscribe it to the processor property, to recalculate
      // the histograms when the processor changes, and to the various constants
      // that don't need to be properties because they never change, and
      // initialize the data in the bee chart, because moving the simulator
      // computation to a background thread means that the data is not populated
      // when the UI first displays. The, subscribe the data to the simulatorData
      // property.
      beeCharts = new Array[BeeChartControl](newSimulation.generator.numberOfFamilies)
      simulatorDataSubscriptions = Some(new Array[Subscription](newSimulation.generator.numberOfFamilies))
      beeChartsBox.children.clear()
      for (i <- 0 until newSimulation.generator.numberOfFamilies) {
        val loader = new FXMLLoader(getClass.getResource("beechart.fxml"))
        val view = loader.load[LineChartWithIndicator]()
        val newBeeChartController = loader.getController[BeeChartControl]()
        newBeeChartController.processor <== processor
        newBeeChartController.minimumTemperature <== minimumYBoundary
        newBeeChartController.maximumTemperature <== maximumYBoundary
        newBeeChartController.numberOfBins() = NumberOfBins
        newBeeChartController.simulatorData() = None
        newBeeChartController.sharedMaximum <== beeChartMaximum
        newBeeChartController.maximumValue.onChange({
          beeChartMaximum() = beeCharts.filter(_ != null).map(_.maximumValue()).maxOption.getOrElse(0.0)
        })

        (simulatorDataSubscriptions.get)(i) = simulatorData.onChange((_, _, newDatasets) => {
          view.indicatorPosition() = Some(newDatasets.get(i).indicatorPosition)
          view.indicatorBand() = newDatasets.get(i).indicatorBand
          newBeeChartController.simulatorData() = Option(newDatasets.get(i))
        })

        view.idProperty.set(s"beechart-${i}")
        beeChartsBox.children.add(view)
        beeCharts(i) = (newBeeChartController)
      }

      // Subscribes the update of simulatorData to the movement of the slider.
      // When the slider is changed, the data is refreshed. This would really only
      // be the two lines:
      //   simulatorData().clear()
      //   simulatorData().addall(datasets)
      // except that, the simulator taking a bit long, the mouse loses track of
      // the slider for a bit while it updates. So the recomputation is done in
      // another thread by a ConflatingRunner, which only runs one simulation at
      // a time and skips slider positions that were passed over while it was
      // busy, and the results are "repatriated," kind-of, to the JavaFX thread
      // with Platform.runLater. While dragging, the charts update as fast as
      // the simulations can be run, and they always end up showing the final
      // slider position.
      simulationRunner.foreach(_.close())
      val runner = new ConflatingRunner[(Double, Double), List[Dataset]](
        (forcingValues) => newSimulation.generator.generatePoints(forcingValues._1, forcingValues._2),
        (datasets) => {
          showError(None)
          simulatorData() = ObservableBuffer.from(datasets)
        },
        () => borderPane.parent().setCursor(jfxs.Cursor.DEFAULT),
        (e) => {
          showError(Some(s"The simulation failed, so the charts weren't updated: ${e}"))
          e.printStackTrace()
        }
      )(backgroundThreads, (task: Runnable) => Platform.runLater(task))
      simulationRunner = Some(runner)
      val heatFlowTooltip = new Tooltip(newSimulation.forcingVariable1Name)
      val heatPerBeeTooltip = new Tooltip(newSimulation.forcingVariable2Name)
      Tooltip.install(heatFlowSlider, heatFlowTooltip)
      Tooltip.install(heatPerBeeSlider, heatPerBeeTooltip)
      heatFlowSlider.min() = newSimulation.forcingVariable1Range._1
      heatFlowSlider.max() = newSimulation.forcingVariable1Range._2
      heatPerBeeSlider.min() = newSimulation.forcingVariable2Range._1
      heatPerBeeSlider.max() = newSimulation.forcingVariable2Range._2
      def updateData(): Unit = {
        borderPane.parent().setCursor(jfxs.Cursor.WAIT)
        runner.submit((heatFlowSlider.value().doubleValue(), heatPerBeeSlider.value().doubleValue()))
      }
      heatFlowSlider.value() = newSimulation.forcingVariable1InitialValue
      heatPerBeeSlider.value() = newSimulation.forcingVariable2InitialValue
      heatFlowSliderSubscription = Some(heatFlowSlider.value.onChange(updateData()))
      heatPerBeeSliderSubscription = Some(heatPerBeeSlider.value.onChange(updateData()))

      // This triggers the data creation. It can't be left to the slider change
      // events, because a slider that is already at its initial value, as the
      // heat per bee slider is when switching between simulations that share
      // the same initial value, doesn't send one.
      updateData()

      // Sets the focus on the slider. This needs to be done "later", after the
      // window is displayed, otherwise it will just get ignored.
      Platform.runLater({
        heatFlowSlider.requestFocus()
      })
    })

    // This statement is guarded, but it is what sets everything else in motion.
    // So, if the simulator combo box isn't initialized yet, it won't work well.
    if (simulatorSelectionComboBox.items().size() > 0) {
      simulatorSelectionComboBox.value() = simulatorSelectionComboBox.items().get(0)
    }
  }

}
