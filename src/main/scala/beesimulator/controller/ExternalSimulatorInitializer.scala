package beesimulator.controller

import beesimulator.bees._
import beesimulator.controller.generators._
import beesimulator.ui._
import beesimulator.simulator._

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
        heatFlowPerBeeLow,
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
    val datasetCreator = (forcingValue: Double, lastTemperature: Double, name: String, numBees: Int, temperatures: List[Double], histogramData: Seq[BeeHistogramItem]) =>
        Dataset(name, forcingValue, numBees, temperatures, histogramData)

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
    )(externalSimProvider)
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
    )(externalSimProvider)
    aggregateGenerator.addDataGenerator(singleFatherInMiddleGenerator)

    new Simulation() {
      val forcingVariableName = "External Temperature"
      val generator = aggregateGenerator
      val forcingVariableRange = (20.0, 23.0)
      val forcingVariableInitialValue = 22.0
      val simulatorName = "External Temperature Simulation"
    }
  }
}
