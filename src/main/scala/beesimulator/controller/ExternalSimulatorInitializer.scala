package beesimulator.controller

import beesimulator.bees._
import beesimulator.controller.generators._
import beesimulator.ui._
import beesimulator.simulator._

/**
 * Initializes the simulation wherein the bees react to the external
 * temperature of the hive.
 */
object ExternalSimulationInitializer extends SimulationInitializer {
  val InitialExternalTemperature = 22
  val ThermalConductivity = 2.3

  def getSimulation(
      startTemperature: Double,
      temperatureRange: Double,
      mass: Double,
      specificHeat: Double
  ): Simulation = {
    val heatFlowPerBeeLow = 0.001
    val heatFlowPerBeeHigh = 0.002

    val externalInitialState =
      ExternalSimState(
        mass,
        specificHeat,
        startTemperature,
        InitialExternalTemperature,
        ThermalConductivity
      )
    val numberOfIterations = 250
    val fatherDifference: Double = 1.5
    val numberOfFathers = 40
    val fatherWindow: Double = 1.5
    val numberOfDaughters = 80

    implicit val externalSimProvider: SimProvider[Double, ExternalSimState, Bee[
      ExternalSimState
    ], ExternalBeeSim] = new ExternalBeeSimProvider(externalInitialState)
    val aggregateGenerator = new AggregateDataGenerator()
    val datasetCreator = (
        forcingValue1: Double,
        forcingValue2: Double,
        lastTemperature: Double,
        name: String,
        numBees: Int,
        temperatures: List[Double],
        histogramData: Seq[BeeHistogramItem]
    ) => Dataset(name, forcingValue1, numBees, temperatures, histogramData)

    val focusedGroups = new FocusedGenerator(
      "Multiple fathers that cluster",
      startTemperature,
      numberOfIterations,
      5,
      640,
      temperatureRange,
      externalInitialState,
      datasetCreator,
      SimpleExternalBee(_)
    )
    aggregateGenerator.addDataGenerator(focusedGroups)

    val mixedFathersGenerator = new MixedFathersGenerator(
      "Mixed Fathers",
      startTemperature,
      numberOfIterations,
      numberOfFathers,
      numberOfDaughters,
      fatherDifference,
      fatherWindow,
      externalInitialState,
      datasetCreator,
      SimpleExternalBee(_)
    )(using externalSimProvider)
    aggregateGenerator.addDataGenerator(mixedFathersGenerator)

    val singleFatherInMiddleGenerator = new SingleFatherGenerator(
      "Single Father with medium thresholds",
      startTemperature,
      numberOfIterations,
      numberOfFathers,
      numberOfDaughters,
      fatherWindow,
      startTemperature + (fatherDifference / 2),
      externalInitialState,
      datasetCreator,
      SimpleExternalBee.apply(_)
    )(using externalSimProvider)
    aggregateGenerator.addDataGenerator(singleFatherInMiddleGenerator)

    new Simulation() {
      val forcingVariable1Name = "External Temperature"
      val forcingVariable2Name = "Heat Flow per Bee"
      val generator = aggregateGenerator
      val forcingVariable1Range = (20.0, 23.0)
      val forcingVariable2Range = (0.001, 0.002)
      val forcingVariable1InitialValue = 22.0
      val forcingVariable2InitialValue = heatFlowPerBeeLow
      val simulatorName = "External Temperature Simulation"
    }
  }
}
