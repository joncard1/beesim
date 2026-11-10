package beesimulator

import beesimulator.controller._
import beesimulator.ui.Simulation

/**
  * Times how long each simulation takes to generate its data, which is how
  * long the UI waits after a slider moves. Run with:
  *
  *   sbt "Test/runMain beesimulator.SimulationBenchmark"
  *
  * The first runs are slower while the JVM compiles the hot code, so they are
  * discarded.
  */
object SimulationBenchmark {
  val WarmupRuns = 10
  val MeasuredRuns = 20

  def main(args: Array[String]): Unit = {
    val simulations = Seq(
      InternalSimulationInitializer.getSimulation(
        BeeSimulatorApp.StartTemperature,
        BeeSimulatorApp.TemperatureRange,
        BeeSimulatorApp.Mass,
        BeeSimulatorApp.SpecificHeat
      ),
      ExternalSimulationInitializer.getSimulation(
        BeeSimulatorApp.StartTemperature,
        BeeSimulatorApp.TemperatureRange,
        BeeSimulatorApp.Mass,
        BeeSimulatorApp.SpecificHeat
      )
    )
    for (simulation <- simulations) {
      measure(simulation)
    }
  }

  private def measure(simulation: Simulation): Unit = {
    val (low1, high1) = simulation.forcingVariable1Range
    val forcing2 = simulation.forcingVariable2InitialValue
    // Vary the input the way a slider drag would, so nothing can be reused
    // between runs that a real drag couldn't reuse.
    def run(i: Int): Unit = {
      val forcing1 = low1 + (high1 - low1) * (i % 10) / 10
      simulation.generator.generatePoints(forcing1, forcing2)
    }
    (0 until WarmupRuns).foreach(run)
    val times = (0 until MeasuredRuns)
      .map(i => {
        val start = System.nanoTime()
        run(i)
        (System.nanoTime() - start) / 1e6
      })
      .sorted
    println(
      f"${simulation.simulatorName}%-35s median ${times(times.size / 2)}%7.1f ms   min ${times.head}%7.1f ms   max ${times.last}%7.1f ms"
    )
  }
}
