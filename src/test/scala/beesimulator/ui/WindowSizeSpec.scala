package beesimulator.ui

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class WindowSizeSpec extends AnyWordSpec with Matchers {
  "The window size" when {
    "the screen has room" should {
      "be the preferred 16:10 size" in {
        WindowSize.forScreen(2560, 1415) shouldBe (1440.0, 900.0)
      }
    }

    "the screen is too small" should {
      "shrink to fit, keeping the aspect ratio" in {
        val (width, height) = WindowSize.forScreen(1280, 777)
        height should be <= 0.9 * 777
        width should be <= 0.9 * 1280
        width / height shouldBe (1.6 +- 0.01)
      }
    }
  }
}
