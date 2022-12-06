package beesimulator.ui

trait Simulation {
  val forcingVariable1Name: String
  val forcingVariable2Name: String
  val generator: DataGenerator
  val forcingVariable1Range: (Double, Double)
  val forcingVariable2Range: (Double, Double)
  val forcingVariable1InitialValue: Double
  val forcingVariable2InitialValue: Double
  val simulatorName: String
}
