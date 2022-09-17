package beesimulator.ui

import scala.annotation.tailrec
import scala.swing._
import scala.swing.BorderPanel.Position
import scala.concurrent.Future
import scala.util._
import java.awt.Color
import rx.lang.scala._
import rx.lang.scala.subjects._
import scala.math._
import beesimulator.bees._
import beesimulator.utils._
import beesimulator.ui.processors._

import scala.concurrent.ExecutionContext.Implicits.global
import java.awt.event.ComponentListener
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent

/**
 * This defines the main window containing the data to display. This consists of
 * an array of charts across the top that summarize the various bee threshold
 * distributions and a large chart displaying the temperature data.
 * 
 * @param minimumYValue     The minimum Y value of the temperature data display.
 *    The y-value at the x-axis. The chart will not adjust to display
 *    temperatures below this value.
 * @param minimumYBoundary  The value that the temperature chart displays as the
 *    minimum expected values produced during the simulation.
 * @param maximumYBoundary  The value that the temperature chart displays as the
 *    maxmium expected value produced during the simulation. The chart will
 *    dynamically adjust to display temperatures above this value.
 * @param generator         The data generator to create new temperature data
 *    when requested by changing the heatIn value with the slider.
 */
class MainFrame(
    minimumYValue: Double,
    minimumYBoundary: Double,
    maximumYBoundary: Double,
    generator: DataGenerator
) extends swing.MainFrame {
  enum ComboSelections {
    case Histogram
    case Cumulative
  }

  /**
   *  The value of the heatIn parameter. This is adjustable with the slider.
   *  This is expressed according to the slider's reference so that I only need
   *  to write one converter, from slider-domain values to energy-domain values.
   *  Plus, it's good practice to keep display code in display data.
   */
  val initialSliderValue = 1
  /**
   *  This list of colors assigned, round-robin style, to the datasets when
   *  displaying them.
   */
  val colors = List(Color.blue, Color.cyan, Color.pink, Color.magenta, Color.orange, Color.yellow, Color.green)

  /**
   *  The observable that will produce temperature data to the temperature
   *  chart. This will subscribe and unsubscribe to the data sources as the data
   *  is regenerated with new heatIn values.
   * 
   * This does not originate the data; @see{temperatureObservable} holds the
   * refernece to the current instance that is originating the data. This is for
   * others to subscribe to and this component to publish to.
   */
  val temperatureData = Subject[List[(Color, Seq[Double])]]()
  /**
   *  The subscription that contains the subscription from the temperatureData
   *  subject to the source of temperature data. These sources are generally
   *  one-time events, so this may not be necessary, but it's good memory
   *  management. I'm unclear on whether completed Observables are simply
   *  releasedall subscribers and dispose of themselves.
   */
  var temperatureChainSubscription: Subscription = Observable.never.subscribe(temperatureData)
  
  /**
   *  An array Subjects to publish data to for the bee charts to consume. The
   *  charts subscribe to these and will react to data passed up these chains.
   */
  val transformedBeeData: Array[Subject[List[(Color, Seq[Int])]]] = new Array(generator.numberOfFamilies)
  /**
   *  An array of subscriptions relating to the subjects in
   *  @see{transformedBeeData}. This is for the corresponding subjects in
   *  transfomedBeeData to unsubscribe from chains that are no longer of
   *  interest. This may be redundant; I'm not sure if completed Observables are
   *  simply dropped from the observer when they are completed.
   */
  val transfomedBeeDataSubscriptions: Array[Subscription] = new Array(generator.numberOfFamilies)
  val beeNameSubjects: Array[Subject[String]] = new Array(generator.numberOfFamilies)
  val beeNameSubscriptions: Array[Subscription] = new Array(generator.numberOfFamilies)
  val finalTemperatureSubjects: Array[Subject[Double]] = new Array(generator.numberOfFamilies)
  val finalTemperatureSubscriptions: Array[Subscription] = new Array(generator.numberOfFamilies)
  val finalBeeCountSubjects: Array[Subject[Int]] = new Array(generator.numberOfFamilies)
  val finalBeeCountSubscriptions: Array[Subscription] = new Array(generator.numberOfFamilies)

  /**
   *  The minimum temperature at which various charts reference their data. The
   *  temperature chart draws a horizontal line at this temperature and the bee
   *  charts take this as the x-value at the y-axis.
   * 
   *  It's basically a constant; it was defined as an Observable when I thought
   *  they might change.
   */
  val temperatureMinimumYBoundary = new Signal(Observable.just(Option(minimumYBoundary)))
  /**
   *  The maximum temperature at which various charts reference their data. The
   *  temperature chart draws a horizontal line at this temperature and the bee
   *  charts take this as the far right boundary.
   * 
   *  It's basically a constant; it was defined as an Observable when I thought
   *  they might change. 
   */
  val temperatureMaximumYBoundary = new Signal(Observable.just(Option(maximumYBoundary)))
  /**
   *  Stores the current originator of the temperature data from the simulator.
   *  This instance is not permanent; it is replaced every time the data needs
   *  to be regenerated. This reference is kept to re-create the processing
   *  chain when the BeeDataProcessor changes so that the data does not need to
   *  be recreated.
   * 
   *  This Observable should be concatenated with Never so that the completion
   *  of the data generation from the simulator does not complete all of the
   *  downstream elements.
   * 
   *  See @see{createDataNewDatasets} for the method that constructs new
   *  intances stored here.
   */
  var temperatureObservable: Observable[List[(Color, BeeDataset)]] = Observable.never

  title = "Bees"
  preferredSize = new Dimension(500, 500)

  // Initializing the temperature chart
  val temperatureChart = new TemperatureChart(minimumYValue) {
    preferredSize = new Dimension(400, 400)
  }
  temperatureChart.setData(temperatureMinimumYBoundary, temperatureMaximumYBoundary, new Signal(temperatureData))

  // Initializing the bee charts
  var beeCharts = new Array[BeeChart](generator.numberOfFamilies)
  var beeNames = new Array[TextArea](generator.numberOfFamilies)
  for (i <- 0 until generator.numberOfFamilies) {
    val newBeeChart = new BeeChart(){
      preferredSize = new Dimension(150, 150)
    }
    beeCharts(i) = newBeeChart

    transformedBeeData(i) = Subject()
    transfomedBeeDataSubscriptions(i) = Observable.never.subscribe(transformedBeeData(i))
    newBeeChart.setData(
      temperatureMinimumYBoundary,
      temperatureMaximumYBoundary,
      new Signal(transformedBeeData(i))
    )

    finalTemperatureSubjects(i) = Subject()
    finalTemperatureSubscriptions(i) = Observable.never.subscribe(finalTemperatureSubjects(i))
    newBeeChart.setFinalTemperature(new Signal(finalTemperatureSubjects(i)))

    finalBeeCountSubjects(i) = Subject()
    finalBeeCountSubscriptions(i) = Observable.never.subscribe(finalBeeCountSubjects(i))
    newBeeChart.setFinalBeeCount(new Signal(finalBeeCountSubjects(i)))

    beeNameSubjects(i) = Subject()
    beeNameSubscriptions(i) = Observable.never.subscribe(beeNameSubjects(i)) 
    beeNames(i) = new TextArea() {
      editable = false
      lineWrap = true
      wordWrap = true
      rows_=(2)
      beeNameSubjects(i).subscribe(name => {
        text = name
      })
    }

  }

  //  This is the text area under the slider that displays the heatIn value
  //  selected.
  val textArea = new TextArea("", 1, 5) {
    editable = false
    maximumSize = new Dimension(1000, 16)
    rows_=(1)
    columns_=(3)
  }

  //  The slider to set the heatIn. When its value changes, it regenerates the
  //  data by calling @see{createNewDatasets}
  val slider = new Slider() {
    orientation = Orientation.Vertical
    this.max = 300
    this.min = 1
    this.value = initialSliderValue
    var oldValue = this.value
    textArea.text = scaleSliderValue(value).toString()
    reactions += {
      case x: event.ValueChanged if value != oldValue => {
        oldValue = value
        val newHeatFlow = scaleSliderValue(value)
        temperatureObservable = createNewDatasets(newHeatFlow)
        textArea.text = newHeatFlow.toString()
      }
    }
  }

  //  The combo box to select the display mode of the bee charts: Histogram or
  //  Cumulative.
  val comboBox = new ComboBox(Seq(ComboSelections.Histogram, ComboSelections.Cumulative)) {
    selection.item_=(ComboSelections.Cumulative)
    selection.reactions. += {
      case event.SelectionChanged(x) if (this.selection.item == ComboSelections.Histogram) => {
        for (i <- 0 until beeCharts.size) {
          val beeChart = beeCharts(i)
          transfomedBeeDataSubscriptions(i).unsubscribe()
          val subscriptionTuple = connectRawDataToBees(HistogramBeeDataProcessor, temperatureObservable, i, beeChart.numberOfBins, transformedBeeData(i), beeNameSubjects(i), finalTemperatureSubjects(i), finalBeeCountSubjects(i))
          transfomedBeeDataSubscriptions(i) = subscriptionTuple._1
          beeNameSubscriptions(i) = subscriptionTuple._2
          finalBeeCountSubscriptions(i) = subscriptionTuple._3
        }
      }
      case event.SelectionChanged(x) if (this.selection.item == ComboSelections.Cumulative) => {
        for (i <- 0 until beeCharts.size) {
          val beeChart = beeCharts(i) 
          transfomedBeeDataSubscriptions(i).unsubscribe()
          val subscriptionTuple = connectRawDataToBees(CumulativeCountBeeDataProcessor, temperatureObservable, i, beeChart.numberOfBins, transformedBeeData(i), beeNameSubjects(i), finalTemperatureSubjects(i), finalBeeCountSubjects(i))
          transfomedBeeDataSubscriptions(i) = subscriptionTuple._1
          beeNameSubscriptions(i) = subscriptionTuple._2
          finalTemperatureSubscriptions(i) = subscriptionTuple._3
          finalBeeCountSubscriptions(i) = subscriptionTuple._4
        }
      }
    }
  }

  contents = new BorderPanel() {
    peer.addComponentListener(new ComponentAdapter {
      // Right now, there is some weird artifacting because I'm probably not
      // calculating the chart heights well, so this should be recalculated on
      // resize. It also stands in for the data initialization. TODO: Maybe I
      // should only re-subscribe the charts to the data instead of
      // recalculating.
      override def componentResized(e: ComponentEvent): Unit = {
        temperatureObservable = createNewDatasets(scaleSliderValue(slider.value))
      }
    })

    border = Swing.BeveledBorder(Swing.Lowered)
    add(new BoxPanel(Orientation.Vertical) {
      contents += comboBox
      contents += new BoxPanel(Orientation.Horizontal) {
        for (i <- 0 until generator.numberOfFamilies) {
          contents += new BoxPanel(Orientation.Vertical) {
            minimumSize = new Dimension(1, 1)
            contents += beeCharts(i)
            contents += beeNames(i)
          }
        }
      }
    }, Position.North)
    add(new BoxPanel(Orientation.Vertical) {
      contents += temperatureChart
    }, Position.Center)
    add(new BoxPanel(Orientation.Vertical) {
      contents += slider
      contents += textArea
    }, Position.East)
    
  }

  /**
   * Minor utility function to capture the conversion from the slider-domain
   * values [1, 100] to energy-domain values [0.01, 3.00]
   *
   * @param sliderVal The value from the slider
   * @return          The energy value to send to the data generators
   */
  def scaleSliderValue(sliderVal: Int) : Double = sliderVal.asInstanceOf[Double] / 100

  /**
    * This function constructs the middle-ground between the data from the
    * generators and the subjects owned by the bee charts. This has to be done
    * whenever either changing the BeeDataProcessor or regenerating the data.
    * 
    * Even though some of these variables are within the scope of the object,
    * this function only uses the data passed in. It is not truly "functional",
    * because as the beeSubject is altered, but it's close.
    *
    * @param processor        The {@see BeeDataProcessor} used to convert the
    *                         data from a collection of bees to a histogram-like
    *                         dataset
    * @param temperatureData  The Observable that produces the generated data
    * @param index            The index of the bee chart the data should be linked to.
    * @param numberOfBins     The number of bins expected in the results.
    * @param beeSubject       The Subject the data should be published to.
    * @return                 The subscription that results from the new chain. This is used to unsubscribe when the chain has to change.
    */
  def connectRawDataToBees(
      processor: BeeDataProcessor,
      temperatureData: Observable[List[(Color, BeeDataset)]],
      index: Int,
      numberOfBins: Int,
      beeSubject: Subject[List[(Color, Seq[Int])]],
      nameSubject: Subject[String],
      finalTemperatureSubject: Subject[Double],
      finalBeeCountSubject: Subject[Int]): (Subscription, Subscription, Subscription, Subscription) = {
    val beeObservable = temperatureData.map(data => data(index)) // Extract one particular dataset
    val chartSubscription =  beeObservable.map(pair => { List((pair._1, processor.transform(minimumYBoundary, maximumYBoundary, numberOfBins, pair._2.bees))) }) // Add the current bee processor to the chain
      .subscribe(beeSubject)    // Subscribe the observable the chart is observing to the new data

    val nameSubscription = beeObservable.map(pair => pair._2.name).subscribe(nameSubject)

    val finalTemperatureSubscription = beeObservable.map(pair => pair._2.finalTemperature).subscribe(finalTemperatureSubject)
    val finalBeeCountSubscription = beeObservable.map(pair => pair._2.finalBeeCount).subscribe(finalBeeCountSubject)
    (chartSubscription, nameSubscription, finalTemperatureSubscription, finalBeeCountSubscription)
  }

  /**
    * This creates the new data generation chain based on the new quantity of
    * heat flow. It spawns a Future to generate the data from the simulator,
    * then builds a chain to operate when that is complete to assign colors to
    * the resulting datasets and forward the temperature data to the temperature
    * chart and the bee data to the histogram-like charts, through the currently
    * selected processor.
    *
    * @param heatFlow The current value in the energy-domain to use when generating from the simulator.
    * @return         The new value of @see{temperatureObservable}
    */
  def createNewDatasets(heatFlow: Double) = {
    // Create new observable with the raw data from the simulator
    val temperatureObservable = Observable.from(Future({
      generator.generatePoints(heatFlow)
    })).map(initialTemperatureData => {
      var charts = beeCharts

      @tailrec
      def assignColors(datasets: List[BeeDataset], colors: List[Color], results: List[(Color, BeeDataset)]): List[(Color, BeeDataset)] = {
        colors match {
          case Nil => assignColors(datasets, this.colors, results)
          case color :: remainingColors => {
            datasets match {
              case Nil => results
              case data :: remainingDatasets => {
                assignColors(remainingDatasets, remainingColors, (color, data) :: results)
              }
            }
          }
        }
      }
          
      assignColors(initialTemperatureData, colors, List())
    }).cache(1) ++ Observable.never

    // The following two sections are done separately because we are essentially
    // forking the data paths.

    // Update the connection to the Observable observed by the temperature
    // chart.
    temperatureChainSubscription = temperatureObservable.map(data => {
      data.map(pair => {
        (pair._1, pair._2.temperatures)
      })
    }).subscribe(temperatureData)

    // Update the connections to the Observables observed by the charts showing
    // the bee thresholds.
    val processor = comboBox.selection.item match {
      case e if e == ComboSelections.Histogram => HistogramBeeDataProcessor
      case e if e == ComboSelections.Cumulative => CumulativeCountBeeDataProcessor
      case _ => throw new IllegalStateException("Combo box selection was not one of the legal values.")
    }
    for (i <- 0 until generator.numberOfFamilies) {
      val beeChart = beeCharts(i)
      
      transfomedBeeDataSubscriptions(i).unsubscribe()
      beeNameSubscriptions(i).unsubscribe()
      finalTemperatureSubscriptions(i).unsubscribe()
      finalBeeCountSubscriptions(i).unsubscribe()

      val subscriptionTuple = connectRawDataToBees(processor, temperatureObservable, i, beeChart.numberOfBins, transformedBeeData(i), beeNameSubjects(i), finalTemperatureSubjects(i), finalBeeCountSubjects(i))
      transfomedBeeDataSubscriptions(i) = subscriptionTuple._1
      beeNameSubscriptions(i) = subscriptionTuple._2
      finalTemperatureSubscriptions(i) = subscriptionTuple._3
      finalBeeCountSubscriptions(i) = subscriptionTuple._4
    }
    temperatureObservable
  }
  
  // Necessary functions to complete the creation of the UI element.
  pack()
  centerOnScreen()
  open()
}