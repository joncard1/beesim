package beesimulator.ui

import scala.swing._
import java.awt.geom._
import java.awt._
import scala.math.BigDecimal

/**
  * This component is used to specifically display the main temperature chart.
  * It main special functionality is to draw the expected temperature
  * boundaries.
  *
  * @param xAxisValue The value of the y-axis at the x-axis. That is, the lowest temperature value displayed on the chart
  */
class TemperatureChart(xAxisValue: Double = 0) extends Chart[Double](xAxisValue) {
  leftPadding = 20
  rightPadding = 20
  topPadding = 20
  bottomPadding = 20

  override protected def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)

    def drawTemperatureLine(temperature: Double) = {
      columnUnitHeight.map(columnUnitHeight => {
        g.setColor(Color.red)
        val x1 = leftPadding
        val y1 = math.round(size.height - bottomPadding - columnUnitHeight * (temperature - xAxisValue)).toInt
        val x2 = size.width - rightPadding
        val y2 = math.round(size.height - bottomPadding - columnUnitHeight * (temperature - xAxisValue)).toInt
        g.drawLine(x1, y1, x2, y2)
      })
    }
    if (this.maximumBoundary.isDefined) {
      this.maximumBoundary.get.foreach(drawTemperatureLine(_))
    }
    if (this.minimumBoundary.isDefined) {
      this.minimumBoundary.get.foreach(drawTemperatureLine(_))
    }
  }
}
