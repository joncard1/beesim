# Roadmap

Concrete work items are tracked as [GitHub issues](https://github.com/joncard1/beesim/issues). Issues labelled `author-task` are for the author to do (for example choosing the license and supplying citations) and are not worked on by Claude.

This file holds the longer-term direction, open questions that need answering before they can become issues, and notes worth keeping.

## Future features

- **Show the equilibrium on the cumulative chart.** For simple bees, the settled state is where the cumulative distribution reaches heat in ÷ heat per bee, so a horizontal line at that height would show the indicator crossing it, which is the core idea of the book. With many populations on screen this would be cluttered; it becomes useful if the UI evolves to let the reader limit which populations are shown.

- **There should be a population that is just a normal distribution** This is the most likely actual shape for a population of bees, based on my limited knowledge of genetics. It would be good to have it as a comparison.

- **There should be a population with the improved integrals, but with half of the population holding a different "correct" temperature** I wonder what the fight would look like.

- **You should be able to select which populations are shown** The chart is cluttered in some places.

- **Reorder the populations** This is probably dumb, but drag-and-drop the charts around.

- **Make the number of steps adjustable** There is stuff happening with regard to derivatives (for instance) that are being obscured by how many steps are being calculated and shown. I believe this will require 1. moving the number of iterations out of InternalBeeSim and ExternalBeeSim and into (probably) a default value of 250 along with MinIterations and MaxIterations (maybe 10 to 250) in either MainController or BeeSimulatorApp, 2. moving numberOfIterations into DataGenerator#generatePoints and removing it from the constructor of AbstractDataGenerator and the other generators, 3. adding a slider control to beecharts.fxml under the main data chart that is initialized to the default, ranges from the minimum to the maximum, increments in whole number steps, has a tooltip of "Steps", and when changed triggers a re-evaluation of the chart data.

- **Label the axes on the population summary charts** Probably a label on the whole section. Also, not sure if the labels on the population charts work as well as showing the legend would be. Not sure all of the charts need to have the axes labelled, but something needs to indicate what those numbers mean.

- **Add check to addSimulation** I wonder if there needs to be a check added to addSimulation to throw an error if setData is called? Purely for safety in case users extend the app. It might not need it; it might only need a guard not to call setData until there's at least on simulation added.

- **Move some things to initialize?** I wonder if there's benefit to moving some initialization code from MainController#setData to MainController#initialize (the FXML function that doesn't exist here)? It seems like a lot of it isn't about setting the initial values; they're about initializing the various listeners, which seems like not on-topic. I bet simulatorDataSubscriptions, heatFlowSliderSubscription, and heatPerBeeSliderSubscription should at least move up to being private/protected var properties of MainController. And I wonder why simulationRunner is updated in simulator change event, suggesting that there's not any danger of a race condition updating it, but then why does updateData need to be defined inside the change listener? If updateData can move out, the slider subscriptions don't need to be cancelled and that looks cleaner. The slider tooltips needs to be updated in response to the simulator change. The ranges of the sliders change, too. That maybe means that there are some changes to the sliders that should result in recomputations and some shouldn't, especially during the process of changing simulators, so maybe that makes sense. The bee charts depend on the value of maximumYBoundary being set, so maybe that means setting the change listener on the simulator selection to after that has been initialized is better than setting up the listener and potentially it activating when the property has an invalid value. I wonder if minimumYValue and minimumYBoundary are now duplicates? One is setting the minimum on the main chart, and the other is setting the minimum on the population charts (so without the padding).

## Open questions

- **Slider ranges.** At the top of its range, the heat in slider doesn't reach the extreme behaviour of most populations. Questions to answer before this becomes an issue:
  - What should the top of each range show: every bee active (saturation), or the temperature running away once the bees can't keep up?
  - Should the ranges be derived from the populations instead of fixed? For example, all ~3,300 bees removing 0.002 each balance a heat in of about 6.6, versus the current maximum of 3.0.
  - The temperature axis is fixed at 19.8–23°, so runs beyond the populations' thresholds would leave the chart. Should the axis grow with the data?
  - In theory the simulations respond to changing the constants in `BeeSimulatorApp`, but that has never been tried. Is that an intended way to experiment, and should it be tested?
- **Further static analysis in CI.** scalafmt and coverage are issues. Other candidates, in rough order of effort: compiler warnings as errors (`-Wunused:all`, `-Werror`), scalafix lint rules, WartRemover. Which, if any, are worth the noise for this project?

## Notes

### Performance

A full internal-simulation run takes about 50 ms (measure with `sbt "Test/runMain beesimulator.SimulationBenchmark"`), and each update is bound by the slowest single generator. If more speed is ever needed:

- Stop a simulation early once it has settled and fill the rest of the series with the settled value (doesn't help oscillating runs).
- Run fewer steps while a slider is being dragged.
- The "abort runs older than one second" rule discussed for `ConflatingRunner` is probably unnecessary at this speed.

Already tried without measurable gain: collecting bees into array buffers instead of lists in `Sim.executeStep`. Profiling shows the time is in the bees' `join`/`leave`/`update` calls, not in collection building, so the memory layout of the bees isn't the bottleneck.

### Watch

- **Monocle** is published only up to 21.0.2 but works with JavaFX 25. A JavaFX upgrade could break it; the UI tests would then fail at startup. A future JavaFX release may include a built-in headless platform that replaces it (not in 25.0.2).
