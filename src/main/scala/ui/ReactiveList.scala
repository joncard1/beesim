package beesimulator.ui

import javafx.collections.ObservableListBase
import rx.lang.scala.Subject
import rx.lang.scala.Observer
import scalafx.collections.ObservableBuffer
import scalafx.application.Platform

object ReactiveList {
}

class ReactiveList[U] extends ObservableBuffer[U] with Observer[Seq[U]] {
  override def onNext(value: Seq[U]): Unit = {
    //Platform.runLater(() => {
      clear()
      addAll(value)
    //})
  }
}
