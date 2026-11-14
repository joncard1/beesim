package beesimulator.ui

import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.scene.chart.{LineChart, NumberAxis}
import javafx.scene.control.ComboBox
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import beesimulator.controller.InternalSimulationInitializer
import scala.jdk.CollectionConverters._

/**
  * Loads the main view with the internal simulation and checks that the bee
  * charts share a y-axis, so that the populations can be compared.
  */
class BeeChartAxesSpec extends AnyWordSpec with Matchers with Eventually {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(20, Seconds), interval = Span(50, Millis))

  "The bee charts" when {
    "showing either summary" should {
      "all use the same y-axis, covering every chart's data" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        val root = JavaFxToolkit.onFxThread {
          val loader = new FXMLLoader(classOf[MainController].getResource("beecharts.fxml"))
          val controller = MainController(19.8, 20.0, 23.0, Seq(InternalSimulationInitializer.getSimulation(20.0, 3.0, 5, 0.5)))
          loader.setController(controller)
          val root: Parent = loader.load()
          new Scene(root, 1200, 800)
          root
        }
        def charts = (0 until 8).map(i => root.lookup(s"#beechart-$i").asInstanceOf[LineChart[Number, Number]])
        eventually {
          JavaFxToolkit.onFxThread {
            root.applyCss()
            root.layout()
            all(charts.map(_.getData.size)) shouldBe 1
          }
        }

        for (summary <- Seq(MainController.ComboSelections.Cumulative, MainController.ComboSelections.Histogram)) {
          JavaFxToolkit.onFxThread {
            root
              .lookup("#processorSelectionComboBoxDelegate")
              .asInstanceOf[ComboBox[MainController.ComboSelections]]
              .setValue(summary)
            root.applyCss()
            root.layout()
            val bounds = charts.map(_.getYAxis.asInstanceOf[NumberAxis].getUpperBound)
            val largest = charts.flatMap(_.getData.get(0).getData.asScala.map(_.getYValue.doubleValue)).max
            withClue(s"$summary: ") {
              bounds.distinct should have size 1
              bounds.head should be >= largest
              bounds.head should be < 2 * largest
            }
          }
        }
      }
    }
  }
}
