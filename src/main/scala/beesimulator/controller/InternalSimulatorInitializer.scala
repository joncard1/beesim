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
    ) => Dataset(name, lastTemperature, numBees, temperatures, histogramData, Dataset.settlingBand(temperatures))

    val mixedFathersGenerator = new MixedFathersGenerator(
      "Mixed Fathers",
      startTemperature,
      numberOfFathers,
      numberOfDaughters,
      fatherDifference,
      fatherWindow,
      internalInitialState,
      datasetCreator
    )(using internalSimProvider)
    aggregateGenerator.addDataGenerator(mixedFathersGenerator)
    val mixedFathersDerivativeGenerator = new MixedFathersGenerator(
      "Mixed Fathers with derivative",
      startTemperature,
      numberOfFathers,
      numberOfDaughters,
      fatherDifference,
      fatherWindow,
      internalInitialState,
      datasetCreator,
      DerivativeBee.apply(_, startTemperature, derivativeLookAhead)
    )
    aggregateGenerator.addDataGenerator(mixedFathersDerivativeGenerator)
    // Two bell curves of equal size, a quarter and three quarters of the way
    // across the temperature range. This was done to see what would happen. On
    // observing the data in the paper Jones et al. (2004), it appeared that
    // there were bee families that stopped participating in the bearding when
    // the temperature got too high. I may have interpreted the data wrongly
    // when drawing that conclusion as the y-axis was in percentages not
    // quantity of bees, so I wanted to see what would happen. It appears this
    // configuration wouldn't produce that result, however; the maintain the
    // lower threshold until some singularity is crossed by a rising Heat In
    // value and then the temperature switches to the higher set point.
    val bimodalBees = numberOfFathers * numberOfDaughters
    val bimodalSigma = temperatureRange / 16
    val bimodalGenerator = new NormalGenerator(
      "Bimodal bee set",
      Seq(
        NormalGenerator.Group(bimodalBees / 2, startTemperature + temperatureRange / 4, bimodalSigma, SimpleBee.apply),
        NormalGenerator.Group(
          bimodalBees - bimodalBees / 2,
          startTemperature + 3 * temperatureRange / 4,
          bimodalSigma,
          SimpleBee.apply
        )
      ),
      internalInitialState,
      datasetCreator
    )
    aggregateGenerator.addDataGenerator(bimodalGenerator)
    val singleFatherInMiddleGenerator = new SingleFatherGenerator(
      "Single Father with medium thresholds",
      startTemperature,
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
      numberOfFathers,
      numberOfDaughters,
      fatherDifference,
      fatherWindow,
      internalInitialState,
      datasetCreator,
      CombinedIntegralBee(_, 22, integralLookAhead)
    )
    aggregateGenerator.addDataGenerator(improvedIntegralGenerator)

    // Normally distributed populations, the same size as the bimodal one.
    for (
      (name, mean, standardDeviation) <- Seq(
        ("Normal distribution, wide", 21.5, 0.75),
        ("Normal distribution, narrow", 21.5, 0.25),
        ("Normal distribution, low", 20.75, 0.75)
      )
    ) {
      aggregateGenerator.addDataGenerator(
        new NormalGenerator(
          name,
          Seq(NormalGenerator.Group(numberOfFathers * numberOfDaughters, mean, standardDeviation, SimpleBee.apply)),
          internalInitialState,
          datasetCreator
        )
      )
    }

    // Like the improved integrals, but half of the bees hold a different
    // "correct" temperature. Both halves have the same, wide, normal
    // distribution of thresholds, so the only difference between them is the
    // temperature each half is trying to reach.
    val twoSetPointsGenerator = new NormalGenerator(
      "Normal improved integrals with two set points",
      Seq(21.0, 22.0).map(controlPoint =>
        NormalGenerator.Group(
          numberOfFathers * numberOfDaughters / 2,
          21.5,
          0.75,
          CombinedIntegralBee(_, controlPoint, integralLookAhead)
        )
      ),
      internalInitialState,
      datasetCreator
    )
    aggregateGenerator.addDataGenerator(twoSetPointsGenerator)

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
