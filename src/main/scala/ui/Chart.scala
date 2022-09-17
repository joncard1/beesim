package beesimulator.ui

import scala.swing._
import java.awt.Color
import scala.annotation.tailrec
import math.Ordered.orderingToOrdered
import scala.reflect.ClassTag
import math.Numeric.Implicits.infixNumericOps
import math.Integral.Implicits.infixIntegralOps
import math.Fractional.Implicits.infixFractionalOps
import math.Ordering.Implicits.infixOrderingOps
import beesimulator.bees.Bee._
import beesimulator.utils._
import beesimulator.utils.Extractor._

/**
  * The general class for all of the charts. It is genericized for different
  * kinds of values, so as long as there is an [[Ordering]] and an [[Extractor]]
  * for the type, it should display it. The chart is instantiated with the value
  * of the y-axis where it intersects the x-axis, and then the data is provided
  * in the form of a [[Signal]] to [[setData]].
  *
  * @param xAxisValue The lowest y-axis value displayed on the chart
  * @param witness1   An [[Ordering]] for the chart's type so that the maxima and minima of the data can be found
  * @param witness2   An [[Extractor]] for the chart's type to convert whatever the type is to a [[Double]]
  */
class Chart[T: ClassTag](xAxisValue: Double = 0.0)(implicit witness1: Ordering[T], witness2: Extractor[T]) extends scala.swing.Component {
  /**
    * The padding to the left of the drawable space before the border of the Component
    */
  var leftPadding: Int = 0
  /**
    * The padding to the right of the drawable space before the border of the Component
    */
  var rightPadding: Int = 0
  /**
    * The padding above the drawable space before the border of the Component
    */
  var topPadding: Int = 0
  /**
    * The padding below the drawable space before the border of the Component
    */
  var bottomPadding: Int = 0
  /**
    * This can have several meanings, depending on the sub-class. It is not used in this class.
    */
  protected var minimumBoundary: Signal[Option[Double]] = Signal(None)
  /**
    * This can have several meanings, depending on the sub-class. It is not used in this class.
    */
  protected var maximumBoundary: Signal[Option[Double]] = Signal(None) 

  minimumSize = new Dimension(leftPadding + rightPadding + 1, topPadding + bottomPadding + 1)

  /**
    * The data to display in the chart. Set this with [[setData]].
    */
  protected var data: Signal[Seq[(Color, Seq[T])]] = Signal(Seq())

  /**
    * Intake of the data to display on the chart. These values are in [[Signal]]
    * classes so that they can be updated easily as part of a reactive chain.
    * The element in newData is series of data displayed with each datum an x
    * category and the value of the datum the height. Each are displayed using
    * the associated color provided in the data.
    *
    * @param minimumBoundary  See [[minimumBoundary]]
    * @param maximumBoundary  See [[maximumBoundary]]
    * @param newData          The data to display in the chart
    */
  def setData(minimumBoundary: Signal[Option[Double]], maximumBoundary: Signal[Option[Double]], newData: Signal[Seq[(Color, Seq[T])]]) = {
    this.maximumBoundary = maximumBoundary
    this.minimumBoundary = minimumBoundary
    this.data = newData
    this.maximumBoundary.onChange(newBoundary => {
      repaint()
    })
    this.minimumBoundary.onChange(newBoundary => {
      repaint()
    })
    this.data.onChange(newData => {
      repaint()
    })
  }

  /**
    * Gets the length of the longest of the data sequences provided in the
    * parameters.
    *
    * @param data The data to analyze
    * @return
    */
  protected def getLength(data: Seq[(Color, Seq[T])]): Int = { data.foldLeft(0)((maxLength, item) => { if (maxLength < item._2.length) item._2.length else maxLength }) }
  /**
    * The length of the longest data series the chart is set to display.
    *
    * @return
    */
  protected def length = getLength(data.getOrElse(Seq()))
  /**
    * The size of each x-category given the length of the longest data series
    * provided to display in fractional pixels, that is, pixels / x-unit.
    *
    * @return
    */
  protected def columnUnitWidth: Double = (this.size.width - leftPadding - rightPadding).asInstanceOf[Double]/ length
  /**
    * The maximum value in all of the series' provided in the parameters.
    *
    * @param data The data to analyze
    * @return     The maximum value
    */
  protected def getMaxValue(data: Seq[(Color, Seq[T])]): Option[Double] = {
    val temp = data.flatMap(item => item._2)
    if (!temp.isEmpty) {
      Some(temp.reduce((maximum: T, item: T) => {
        item max maximum
      }).extract())
    } else {
      None
    }
  }
  /**
    * The maximum value in all of the data provided for the chart to display.
    *
    * @return
    */
  protected def maxValue: Option[Double] = Option(math.max(maximumBoundary.getOrElse(Some(Double.NegativeInfinity)).getOrElse(Double.NegativeInfinity), getMaxValue(data.getOrElse(Seq())).getOrElse(Double.NegativeInfinity)))
  /**
    * The size of each pixel in the y direction give the maximum value in all of
    * the data series, and the [[xAxisValue]]. It is in fractional pixels, that
    * is, pixel / y-unit.
    *
    * @return
    */
  protected def columnUnitHeight = maxValue.map(maxValue => (size.height - topPadding - bottomPadding).asInstanceOf[Double] / (maxValue - xAxisValue))

  override protected def paintComponent(g: Graphics2D): Unit = {
    def drawAxes() = {
        g.drawLine(leftPadding, this.size.height - bottomPadding, this.size.width - rightPadding, this.size.height - bottomPadding)
        g.drawLine(leftPadding, this.size.height - bottomPadding, leftPadding, topPadding)
    }
    drawAxes()

    for (dataset <- data.getOrElse(Seq())) {
      g.setColor(dataset._1)
      var index = 0
      for (value <- dataset._2) {
        columnUnitHeight.foreach(columnUnitHeight => {
          val circleX = leftPadding + index * columnUnitWidth
          val circleY = size.height - bottomPadding - columnUnitHeight * (value.extract() - xAxisValue)
          val circle = new java.awt.geom.Ellipse2D.Double(
              circleX,
              circleY.toDouble,
              columnUnitWidth,
              columnUnitWidth)
          g.draw(circle)
        })
        index = index + 1
      }
    }
  }
}
