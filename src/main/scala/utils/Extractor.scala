package beesimulator.utils

import beesimulator.bees._

/**
  * Simple utility to extract a Double from any kind of object with an implicit
  * defined below. This was an issue when defining beesimulator.ui.Chart in a
  * type-agnostic fashion, because it needs to compare values when sizing the
  * chart. Because Double and Int have no common parent, and it needed
  * instructions on how to use the threshold when comparing bees, this class's
  * utilities were used.
  */
trait Extractor[T] {
  def extract(item: T): Double

  class InternalExtractor(item: T) {
    def extract(): Double = Extractor.this.extract(item)
  }
}

object Extractor {
  
  implicit object intExtractorConstructor extends Extractor[Int] {
    override def extract(item: Int): Double = item
  }

  implicit object doubleExtractor extends Extractor[Double] {
    override def extract(item: Double): Double = item.toDouble
  }

  implicit object simpleBeeExtractor extends Extractor[SimpleBee] {
    override def extract(item: SimpleBee): Double = item.threshold
  }

  implicit def getExtractor[T](item: T)(implicit ex: Extractor[T]): Extractor[T]#InternalExtractor = new ex.InternalExtractor(item)
}
