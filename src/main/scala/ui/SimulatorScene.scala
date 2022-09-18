package beesimulator.ui

import scalafx.scene._
import scalafx.beans.property.BufferProperty
import scalafx.scene.chart._
import scalafx.scene.chart.XYChart._
import scalafx.scene.layout._
import scalafx.scene.text.Text
import scalafx.scene.control._
import scalafx.geometry._
import scalafx.scene.paint._
import scalafx.scene.paint.Color._
import scalafx.beans.property.Property
import scalafx.beans.property.DoubleProperty
import scalafx.collections.ObservableBuffer
import rx.lang.scala.Observable
import scala.concurrent.Future

import concurrent.ExecutionContext.Implicits.global
import scala.annotation.tailrec
import rx.lang.scala.Observer
import rx.lang.scala.Subscription
import scalafx.application.Platform
import beesimulator.ui.processors.HistogramBeeDataProcessor
import beesimulator.ui.processors.CumulativeCountBeeDataProcessor

class SimulatorScene(
    minimumYValue: Double,
    minimumYBoundary: Double,
    maximumYBoundary: Double,
    generator: DataGenerator
) extends Scene {
  // TODO: the Colors aren't right
  // TODO: Haven't built the custom chart with the vertical line
  // TODO: The background processing still isn't running; it's in the UI thread now.

  /** This list of colors assigned, round-robin style, to the datasets when
    * displaying them.
    */
  //val colors = List(Blue, Cyan, Pink, Magenta, Orange, Yellow, Green)

  enum ComboSelections {
    case Histogram
    case Cumulative
  }

  // Stores the latest data from the simulator
  var simulatorObservable: Observable[Seq[BeeDataset]] = Observable.never

  val temperaturesList =
    ReactiveList[javafx.scene.chart.XYChart.Series[Number, Number]]
  var temperaturesListSubscription: Subscription =
    Observable.never.subscribe(temperaturesList)

  var beeDataLists = new Array[
    ReactiveList[javafx.scene.chart.XYChart.Series[Number, Number]]
  ](generator.numberOfFamilies)
  var beeDataSubscriptions =
    new Array[Subscription](generator.numberOfFamilies)
  val beeCharts =
    new Array[LineChart[Number, Number]](generator.numberOfFamilies)

  val colors = Array("#f9d900", "#a9e200", "#22bad9", "#0181e2", "#2f357f", "#860061", "#c62b00", "#ff5700")
  // Initialize the bee charts and infrastructure
  for (i <- 0 until generator.numberOfFamilies) {
    beeDataLists(i) =
      ReactiveList[javafx.scene.chart.XYChart.Series[Number, Number]]
    beeDataSubscriptions(i) = Observable.never.subscribe(beeDataLists(i))

    val xAxis = new NumberAxis {}
    val yAxis = new NumberAxis { label = "Bee Count" }
    beeCharts(i) = new LineChart[Number, Number](xAxis, yAxis) {
      val ratio = prefWidth / prefHeight
      prefHeight = 200
      prefWidth = 240
      data = beeDataLists(i)
      animated = false
      createSymbols = false
      style = s".series0.chart-series-line { -fxstroke: green; } .series1.chart-series-line { -fxstroke: green; } .series2.chart-series-line { -fxstroke: green; } .default-color3.chart-series-line { -fxstroke: green; } .default-color4.chart-series-line { -fxstroke: green; } .default-color5.chart-series-line { -fxstroke: green; } .default-color6.chart-series-line { -fxstroke: green; } .default-color7.chart-series-line { -fxstroke: green; }"
    }
  }

  val processorSelectionComboBox = new ComboBox(
    Seq(ComboSelections.Histogram, ComboSelections.Cumulative)
  ) {
    editable = false
    value = ComboSelections.Histogram
    hgrow = Priority.Always

    value.onChange { (_, _, newValue) =>
      beeDataSubscriptions = connectBeeCharts(
        simulatorObservable,
        beeDataLists.asInstanceOf[Array[
          Observer[Seq[javafx.scene.chart.XYChart.Series[Number, Number]]]
        ]],
        beeDataSubscriptions
      )
      temperaturesListSubscription = connectTemperatureChart(
        simulatorObservable,
        temperaturesList,
        temperaturesListSubscription
      )


      for (i <- 0 until 8) {
        val temp = beeCharts(i).lookupAll(s".default-color${i}.chart-series-line")
        println(temp)
      }
    }
  }

  content = new StackPane {
    children = Seq(
      new BorderPane {
        fill = new LinearGradient(
          endX = 0,
          stops = Stops(White, DarkGray)
        )

        padding = Insets(20, 20, 20, 20)

        top = new VBox {
          children = Seq(
            processorSelectionComboBox,
            new HBox {
              children = beeCharts
            }
          )
        }
        right = new VBox {
          val slider = new Slider {
            orientation = Orientation.Vertical
            vgrow = Priority.Always
            max = 3.0

            value.onChange((_, _, newValue) => {
              simulatorObservable = refreshData(newValue.doubleValue())
              connectTemperatureChart(simulatorObservable, temperaturesList, temperaturesListSubscription)
              connectBeeCharts(simulatorObservable, beeDataLists.asInstanceOf[Array[Observer[Seq[javafx.scene.chart.XYChart.Series[Number, Number]]]]], beeDataSubscriptions)
            })
          }
          val text = new Text {
            prefHeight = 16
            this.text <== slider.value.asString("%#1.2f")
          }
          children = Seq(slider, text)
        }

        val xAxis = new NumberAxis {
          label = "Time"
        }
        val yAxis = new NumberAxis("Temperature", minimumYValue, maximumYBoundary, 0.1)
        center = new LineChart[Number, Number](xAxis, yAxis) {
          data = temperaturesList
          prefWidth = 500
          animated = false
          createSymbols = false

          /*
          val cssBuilder = new java.lang.StringBuilder()
          for (i <- 0 until generator.numberOfFamilies) {
            cssBuilder.append(s".chart-series-line .series${i}: { default-color${i} }; ")
          }
          style = cssBuilder.toString().trim()
          */
        }
      }
    )
  }

  def refreshData(heatInflux: Double) = {
    Observable.from(//Future {
      Seq(generator.generatePoints(heatInflux))
    ).cache ++ Observable.never
  }

  def connectTemperatureChart(
      temperaturesObservable: Observable[Seq[BeeDataset]],
      temperatures: Observer[
        Seq[javafx.scene.chart.XYChart.Series[Number, Number]]
      ],
      temperaturesListSubscription: Subscription
  ) = {
    temperaturesListSubscription.unsubscribe()

    temperaturesObservable
      .map(datasets => {
        datasets.map(dataset => {
          Series[Number, Number](
            dataset.name,
            ObservableBuffer.from(
              dataset.temperatures.zipWithIndex.map(tuple => {
                XYChart.Data(tuple._2, tuple._1)
              })
            )
          )
        })
      })
      .subscribe(temperaturesList)
  }

  def connectBeeCharts(
      temperaturesObservable: Observable[Seq[BeeDataset]],
      beeDataLists: Array[
        Observer[Seq[javafx.scene.chart.XYChart.Series[Number, Number]]]
      ],
      beeDataSubscriptions: Array[Subscription]
  ): Array[Subscription] = {
    val processor = processorSelectionComboBox.value.value match {
      case x if (x == ComboSelections.Histogram) => HistogramBeeDataProcessor
      case x if (x == ComboSelections.Cumulative) =>
        CumulativeCountBeeDataProcessor
      case _ => throw new IllegalStateException("The selected value is unknown")
    }
    for (i <- 0 until beeDataLists.length) {
      beeDataSubscriptions(i).unsubscribe()
      beeDataSubscriptions(i) = temperaturesObservable
        .map(datasets => datasets(i))
        .map(dataset =>
          // Not crazy about this side-effect here.
          beeCharts(i).title = dataset.name
          Seq(
            Series[Number, Number](
              dataset.name,
              ObservableBuffer.from(
                processor
                  .transform(
                    minimumYBoundary,
                    maximumYBoundary,
                    50,
                    dataset.bees
                  )
                  .zipWithIndex
                  .map(tuple => XYChart.Data(tuple._2, tuple._1))
              )
            )
          )
        )
        .subscribe(beeDataLists(i))
    }
    beeDataSubscriptions
  }

  println(s"Implicit Exit: ${Platform.implicitExit}")
  simulatorObservable = refreshData(1.0)
  temperaturesListSubscription = connectTemperatureChart(
    simulatorObservable,
    temperaturesList,
    temperaturesListSubscription
  )
  beeDataSubscriptions = connectBeeCharts(
    simulatorObservable,
    beeDataLists.asInstanceOf[Array[
      Observer[Seq[javafx.scene.chart.XYChart.Series[Number, Number]]]
    ]],
    beeDataSubscriptions
  )
}
