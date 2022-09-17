package beesimulator.ui

import scala.swing._
import java.awt.geom._
import java.awt._
import math.BigDecimal.double2bigDecimal
import beesimulator.utils._
import javax.swing.text.NumberFormatter
import java.text.NumberFormat

class BeeChart extends Chart[Int] {
  leftPadding = 5
  rightPadding = 5
  topPadding = 5
  bottomPadding = 5
  
  def numberOfBins = size.width - leftPadding - rightPadding

  private var finalTemperature: Signal[Double] = Signal(0.0)
  private var finalBeeCount: Signal[Int] = Signal(0)

  override protected def paintComponent(g: Graphics2D): Unit = {
    super.paintComponent(g)

    if (maximumBoundary.isDefined && minimumBoundary.isDefined) {
      val maximumTemperature = maximumBoundary.get
      val minimumTemperature = minimumBoundary.get
      if (minimumTemperature.isDefined && maximumTemperature.isDefined && finalTemperature.isDefined) {
        val widthInPixels = (size.width - leftPadding - rightPadding)
        val percentage = (finalTemperature.get - minimumTemperature.get) / (maximumTemperature.get - minimumTemperature.get)
        val x = math.floor((widthInPixels * percentage)).rounded.toInt + leftPadding
        g.drawLine(x + leftPadding, bottomPadding, x + leftPadding, this.size.height - topPadding)
      }
      if (finalTemperature.isDefined ) {
        g.setColor(Color.black)
        val formatter = NumberFormat.getInstance()
        formatter.setMinimumFractionDigits(2)
        formatter.setMaximumFractionDigits(2)
        g.drawString(formatter.format(finalTemperature.get), leftPadding + 15, topPadding + 10)
      }
      if (finalBeeCount.isDefined) {
        g.setColor(Color.black)
        g.drawString(finalBeeCount.get.toString(), leftPadding + 15, topPadding + 20)
      }
    }
  }

  def setFinalTemperature(newFinalTemperature: Signal[Double]): Unit = {
    finalTemperature = newFinalTemperature
    finalTemperature.onChange(_ => repaint())
  }

  def setFinalBeeCount(newFinalBeeCount: Signal[Int]): Unit = {
    finalBeeCount = newFinalBeeCount
    finalBeeCount.onChange(_ => repaint())
  }
}
