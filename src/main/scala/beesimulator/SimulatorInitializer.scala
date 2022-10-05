package beesimulator

import beesimulator.bees._
import beesimulator.controller.generators._
import beesimulator.ui.DataGenerator

object SimulatorInitializer {
  def getGenerator(
      startTemperature: Double,
      temperatureRange: Double
  ): DataGenerator = {
    val numberOfIterations = 250
    val mass = 5
    val initialTemperature = 20
    val specificHeat = 0.5
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
    val simAttributes =
      SimAttributes(mass, initialTemperature, specificHeat, heatFlowPerBeeLow)
    val aggregateGenerator = new AggregateDataGenerator()
/*
    val mixedFathersGenerator = new MixedFathersGenerator(
      "Mixed Fathers",
      startTemperature,
      numberOfIterations,
      numberOfFathers,
      numberOfDaughters,
      fatherDifference,
      fatherWindow,
      simAttributes
    )
    aggregateGenerator.addDataGenerator(mixedFathersGenerator)
    val mixedFathersDerivativeGenerator = new MixedFathersGenerator(
      "Mixed Fathers with derivative",
      startTemperature,
      numberOfIterations,
      numberOfFathers,
      numberOfDaughters,
      fatherDifference,
      fatherWindow,
      simAttributes,
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
      simAttributes
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
      simAttributes
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
      simAttributes
    )
    aggregateGenerator.addDataGenerator(singleFatherAtBottomGenerator)
    val focusedGroups = new FocusedGenerator(
      "Multiple fathers that cluster",
      startTemperature,
      numberOfIterations,
      5,
      640,
      temperatureRange,
      simAttributes
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
      simAttributes,
      IntegralBee.apply(_, integralLookAhead)
    )
    aggregateGenerator.addDataGenerator(mixedFathersIntegralGenerator)
    */
    val improvedIntegralGenerator = new MixedFathersGenerator(
      "Mixed fathers with improved integrals",
      startTemperature,
      numberOfIterations,
      numberOfFathers,
      numberOfDaughters,
      fatherDifference,
      fatherWindow,
      simAttributes,
      CombinedIntegralBee(_, 22, integralLookAhead)
    )
    aggregateGenerator.addDataGenerator(improvedIntegralGenerator)

    return aggregateGenerator
  }
}
