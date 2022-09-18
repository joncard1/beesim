package beesimulator.ui

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import scalafx.beans.property.BufferProperty
import org.scalacheck.Gen

import collection.convert.ImplicitConversions._
import org.scalacheck.Arbitrary
import org.scalamock.scalatest.MockFactory
import javafx.collections.ObservableList
import scalafx.beans.value.ObservableValue
import scalafx.collections.ObservableBuffer
import org.scalamock.function.FunctionAdapter3
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

// TODO: This might be broken given the change of thread that has to happen.
class ReactiveListSpec
    extends AnyWordSpec()
    with Matchers
    with ScalaCheckPropertyChecks
    with MockFactory {

  "A ReactiveList" should {

    "be assignable to a ListProperty" in {
      var expectations = List[FunctionAdapter3[
        ObservableValue[ObservableBuffer[Int], ObservableList[Int]],
        ObservableList[Int],
        ObservableList[Int],
        Boolean
      ]]()

      forAll { (list: Seq[Int], valueToAdd: Int) =>
        {
          val reactiveList = new ReactiveList[Int, Seq[Int]]().addAll(list)
          val prop = new BufferProperty[Int](reactiveList)
          val onChangeHandler =
            mockFunction[ObservableValue[
              ObservableBuffer[Int],
              javafx.collections.ObservableList[Int]
            ], ObservableList[Int], ObservableList[Int], Unit]
          onChangeHandler
            .expects(where((_, _, value) =>
              val temp = list :+ valueToAdd
              if (value.size() != temp.size())
                false
              else
                value.zip(temp).forall(tuple => tuple._1 === tuple._2)
            ))
            .once()
          prop.onChange(onChangeHandler)

          reactiveList.onNext(Seq(valueToAdd))
        }
      }
    }
  }

}
