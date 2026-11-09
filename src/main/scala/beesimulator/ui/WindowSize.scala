package beesimulator.ui

/**
  * Chooses the size the main window opens at: a screen-like 16:10 window, as
  * large as the charts comfortably need, shrunk to fit smaller screens.
  */
object WindowSize {

  /**
    * The size to open at on a screen with room for it.
    */
  val PreferredWidth = 1440.0
  val PreferredHeight = 900.0

  /**
    * The largest fraction of the screen's usable area the window may take, so
    * that it never opens larger than the screen.
    */
  val MaximumScreenFraction = 0.9

  /**
    * The size to open the window at.
    *
    * @param screenWidth  The width of the screen's usable area.
    * @param screenHeight The height of the screen's usable area.
    * @return             The window's width and height, in the preferred
    *     aspect ratio.
    */
  def forScreen(screenWidth: Double, screenHeight: Double): (Double, Double) = {
    val scale = Seq(
      1.0,
      MaximumScreenFraction * screenWidth / PreferredWidth,
      MaximumScreenFraction * screenHeight / PreferredHeight
    ).min
    (math.floor(PreferredWidth * scale), math.floor(PreferredHeight * scale))
  }
}
