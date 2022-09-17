package beesimulator

import beesimulator.controller._
import beesimulator.controller.generators._
import beesimulator.bees._
import beesimulator.ui._

/**
  * The main entry point of the application, this handles the assembly of the
  * component parts and their interdependencies, which shapes the execution of
  * the application. This is where to add data generators that create different
  * strategies for the bees, and then launches the main window.
  */
object Main extends App {
  val startTemperature = 20
  val numberOfIterations = 250
  val mass = 5
  val initialHeat = 50
  val specificHeat = 0.5
  val temperatureRange: Double = 3.0
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

  // Use teh heatFlowPerBeeLow or heatFlowPerBeeHigh to see less and more
  // instability. The "fish tail" shapes when using heatFlowPerBeeHigh is
  // actually decreasing oscillations above and below the equilibrium.
  val simAttributes = SimAttributes(mass, initialHeat, specificHeat, heatFlowPerBeeHigh)

  val mixedFathersGenerator = new MixedFathersGenerator(
    "Mixed Fathers",
    startTemperature,
    numberOfIterations,
    numberOfFathers,
    numberOfDaughters,
    fatherDifference,
    fatherWindow,
    simAttributes)
  val mixedFathersDerivativeGenerator = new MixedFathersGenerator(
    "Mixed Fathers with derivative",
    startTemperature,
    numberOfIterations,
    numberOfFathers,
    numberOfDaughters,
    fatherDifference,
    fatherWindow,
    simAttributes,
    DerivativeBee.apply(_, startTemperature, derivativeLookAhead))
  val bimodalGenerator = new BimodalGenerator(
    "Bimodal bee set",
    startTemperature,
    numberOfIterations,
    numberOfFathers,
    numberOfDaughters,
    temperatureRange,
    simAttributes)
  val singleFatherInMiddleGenerator = new SingleFatherGenerator(
    "Single Father with medium thresholds",
    startTemperature,
    numberOfIterations,
    numberOfFathers,
    numberOfDaughters,
    fatherWindow,
    startTemperature + (fatherDifference / 2),
    simAttributes)
  val singleFatherAtBottomGenerator = new SingleFatherGenerator(
    "Single Father with low thresholds",
    startTemperature,
    numberOfIterations,
    numberOfFathers,
    numberOfDaughters,
    fatherWindow,
    startTemperature,
    simAttributes)
  val focusedGroups = new FocusedGenerator(
    "Multiple fathers that cluster",
    startTemperature,
    numberOfIterations,
    5,
    640,
    temperatureRange,
    simAttributes)
  val mixedFathersIntegralGenerator = new MixedFathersGenerator(
    "Mixed Fathers with integrals",
    startTemperature,
    numberOfIterations,
    numberOfFathers,
    numberOfDaughters,
    fatherDifference,
    fatherWindow,
    simAttributes,
    IntegralBee.apply(_, integralLookAhead))
  val improvedIntegralGenerator = new MixedFathersGenerator(
    "Mixed fathers with improved integrals",
    startTemperature,
    numberOfIterations,
    numberOfFathers,
    numberOfDaughters,
    fatherDifference,
    fatherWindow,
    simAttributes,
    CombinedIntegralBee(_, 22, integralLookAhead)) 
  val aggregateGenerator = new AggregateDataGenerator()
  aggregateGenerator.addDataGenerator(mixedFathersGenerator)
  aggregateGenerator.addDataGenerator(mixedFathersDerivativeGenerator)
  aggregateGenerator.addDataGenerator(bimodalGenerator)
  aggregateGenerator.addDataGenerator(singleFatherInMiddleGenerator)
  aggregateGenerator.addDataGenerator(singleFatherAtBottomGenerator)
  aggregateGenerator.addDataGenerator(focusedGroups)
  aggregateGenerator.addDataGenerator(mixedFathersIntegralGenerator)
  aggregateGenerator.addDataGenerator(improvedIntegralGenerator)

  val ui = new MainFrame(startTemperature - 0.2, startTemperature, startTemperature + temperatureRange, aggregateGenerator)
  ui.visible = true
}