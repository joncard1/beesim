package beesimulator.controller

import beesimulator.bees._
import beesimulator.controller.generators._
import beesimulator.ui._
import beesimulator.controller._
import beesimulator.simulator._

/** Initializes the simulation wherein the bees react to the internal
  * temperature of the hive.
  */
object InternalSimulationInitializer extends SimulationInitializer {
  def getSimulation(
      startTemperature: Double,
      temperatureRange: Double,
      mass: Double,
      specificHeat: Double
  ): Simulation = {
    val numberOfIterations = 250
    val heatFlowPerBeeLow = 0.001
    val heatFlowPerBeeHigh = 0.002

    // Meaning, the difference in distributions from different fathers ranges over 1.5 degrees
    val fatherDifference: Double = 1.5
    val numberOfFathers = 40
    // Meaning, daughters of a particular father range over 1.5 degrees
    val fatherWindow: Double = 1.5
    val numberOfDaughters = 80
    val derivativeLookAhead = 2
    val integralLookAhead = 20

    // Use the heatFlowPerBeeLow or heatFlowPerBeeHigh to see less and more
    // instability. The "fish tail" shapes when using heatFlowPerBeeHigh is
    // actually decreasing oscillations above and below the equilibrium.
    val internalInitialState =
      InternalSimState(mass, specificHeat, startTemperature)
    implicit val internalSimProvider: SimProvider[Double, InternalSimState, Bee[
      InternalSimState
    ], InternalBeeSim] = new InternalBeeSimProvider(internalInitialState)

    val aggregateGenerator = new AggregateDataGenerator()
    val datasetCreator = (
        forcingValue1: Double,
        forcingValue2: Double,
        lastTemperature: Double,
        name: String,
        numBees: Int,
        temperatures: List[Double],
        histogramData: Seq[BeeHistogramItem]
    ) => Dataset(name, lastTemperature, numBees, temperatures, histogramData)

    val mixedFathersGenerator = new MixedFathersGenerator(
      "Mixed Fathers",
      startTemperature,
      numberOfIterations,
      numberOfFathers,
      numberOfDaughters,
      fatherDifference,
      fatherWindow,
      internalInitialState,
      datasetCreator
    )(internalSimProvider)
    aggregateGenerator.addDataGenerator(mixedFathersGenerator)
    val mixedFathersDerivativeGenerator = new MixedFathersGenerator(
      "Mixed Fathers with derivative",
      startTemperature,
      numberOfIterations,
      numberOfFathers,
      numberOfDaughters,
      fatherDifference,
      fatherWindow,
      internalInitialState,
      datasetCreator,
      DerivativeBee.apply(_, startTemperature, derivativeLookAhead)
    )
    aggregateGenerator.addDataGenerator(mixedFathersDerivativeGenerator)
    val bimodalGenerator = new BimodalGenerator(
      "Bimodal bee set",
      startTemperature,
      numberOfIterations,
      numberOfFathers,
      numberOfDaughters,
      temperatureRange,
      internalInitialState,
      datasetCreator
    )
    aggregateGenerator.addDataGenerator(bimodalGenerator)
    val singleFatherInMiddleGenerator = new SingleFatherGenerator(
      "Single Father with medium thresholds",
      startTemperature,
      numberOfIterations,
      numberOfFathers,
      numberOfDaughters,
      fatherWindow,
      startTemperature + (fatherDifference / 2),
      internalInitialState,
      datasetCreator
    )
    aggregateGenerator.addDataGenerator(singleFatherInMiddleGenerator)
    val singleFatherAtBottomGenerator = new SingleFatherGenerator(
      "Single Father with low thresholds",
      startTemperature,
      numberOfIterations,
      numberOfFathers,
      numberOfDaughters,
      fatherWindow,
      startTemperature,
      internalInitialState,
      datasetCreator
    )
    aggregateGenerator.addDataGenerator(singleFatherAtBottomGenerator)
    val focusedGroups = new FocusedGenerator(
      "Multiple fathers that cluster",
      startTemperature,
      numberOfIterations,
      5,
      640,
      temperatureRange,
      internalInitialState,
      datasetCreator
    )
    aggregateGenerator.addDataGenerator(focusedGroups)
    val mixedFathersIntegralGenerator = new MixedFathersGenerator(
      "Mixed Fathers with integrals",
      startTemperature,
      numberOfIterations,
      numberOfFathers,
      numberOfDaughters,
      fatherDifference,
      fatherWindow,
      internalInitialState,
      datasetCreator,
      IntegralBee.apply(_, integralLookAhead)
    )
    aggregateGenerator.addDataGenerator(mixedFathersIntegralGenerator)
    val improvedIntegralGenerator = new MixedFathersGenerator(
      "Mixed fathers with improved integrals",
      startTemperature,
      numberOfIterations,
      numberOfFathers,
      numberOfDaughters,
      fatherDifference,
      fatherWindow,
      internalInitialState,
      datasetCreator,
      CombinedIntegralBee(_, 22, integralLookAhead)
    )
    aggregateGenerator.addDataGenerator(improvedIntegralGenerator)

    new Simulation() {
      val forcingVariable1Name = "Heat In"
      val forcingVariable2Name = "Heat Flow per Bee"
      val generator = aggregateGenerator
      val forcingVariable1Range = (0.0, 3.0)
      val forcingVariable2Range = (0.001, 0.002)
      val forcingVariable1InitialValue = 0.1
      val forcingVariable2InitialValue = 0.001
      val simulatorName = "Internal temperature simulation"
    }
  }
}
