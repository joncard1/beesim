package beesimulator.ui

import javafx.fxml.FXMLLoader
import javafx.scene.{Cursor, Parent, Scene}
import javafx.scene.control.{Label, Slider}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

/**
  * Checks that the main view tells the user when a simulation run fails,
  * using a simulation that fails whenever the first slider is below 1.
  */
class ErrorDisplaySpec extends AnyWordSpec with Matchers with Eventually {

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(20, Seconds), interval = Span(50, Millis))

  val failingSimulation: Simulation = new Simulation {
    val forcingVariable1Name = "Heat In"
    val forcingVariable2Name = "Heat Flow per Bee"
    val forcingVariable1Range = (0.0, 3.0)
    val forcingVariable2Range = (0.001, 0.002)
    val forcingVariable1InitialValue = 0.5
    val forcingVariable2InitialValue = 0.001
    val simulatorName = "Sometimes fails"
    val generator = new DataGenerator {
      def numberOfFamilies = 1
      def generatePoints(forcingVariable1: Double, forcingVariable2: Double): List[Dataset] = {
        if (forcingVariable1 < 1) throw new IllegalStateException("heat in is too low")
        List(Dataset("works", 20.5, 0, Seq(20.0, 20.5), Seq()))
      }
    }
  }

  "The main view" when {
    "a simulation run fails" should {
      "show the error, reset the cursor, and clear the error after a successful run" in {
        assert(JavaFxToolkit.available, "JavaFX could not start; see JavaFxToolkit")
        val (root, errorLabel, heatFlowSlider) = JavaFxToolkit.onFxThread {
          val loader = new FXMLLoader(classOf[MainController].getResource("beecharts.fxml"))
          val root: Parent = loader.load()
          val controller: MainController = loader.getController()
          controller.addSimulation(failingSimulation)
          new Scene(root, 1200, 800)
          controller.setData(19.8, 20.0, 23.0)
          (root, root.lookup("#errorLabelDelegate").asInstanceOf[Label], root.lookup("#heatFlowSliderDelegate").asInstanceOf[Slider])
        }

        eventually {
          JavaFxToolkit.onFxThread {
            errorLabel.isVisible shouldBe true
            errorLabel.getText should include("heat in is too low")
            root.getCursor shouldBe Cursor.DEFAULT
          }
        }

        JavaFxToolkit.onFxThread(heatFlowSlider.setValue(2.0))
        eventually {
          JavaFxToolkit.onFxThread {
            errorLabel.isVisible shouldBe false
            errorLabel.isManaged shouldBe false
          }
        }
      }
    }
  }
}
