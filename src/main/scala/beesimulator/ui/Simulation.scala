package beesimulator.ui

trait Simulation {
  val forcingVariableName: String
  val generator: DataGenerator
  val forcingVariableRange: (Double, Double)
  val forcingVariableInitialValue: Double
  val simulatorName: String
}
