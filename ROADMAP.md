# Roadmap

Concrete work items are tracked as [GitHub issues](https://github.com/joncard1/beesim/issues). Issues labelled `author-task` are for the author to do (for example choosing the license and supplying citations) and are not worked on by Claude.

This file holds the longer-term direction, open questions that need answering before they can become issues, and notes worth keeping.

## Future features

- **Show the equilibrium on the cumulative chart.** For simple bees, the settled state is where the cumulative distribution reaches heat in ÷ heat per bee, so a horizontal line at that height would show the indicator crossing it, which is the core idea of the book. With many populations on screen this would be cluttered; it becomes useful if the UI evolves to let the reader limit which populations are shown.

## Open questions

- **Slider ranges.** At the top of its range, the heat in slider doesn't reach the extreme behaviour of most populations. Questions to answer before this becomes an issue:
  - What should the top of each range show: every bee active (saturation), or the temperature running away once the bees can't keep up?
  - Should the ranges be derived from the populations instead of fixed? For example, all ~3,300 bees removing 0.002 each balance a heat in of about 6.6, versus the current maximum of 3.0.
  - The temperature axis is fixed at 19.8–23°, so runs beyond the populations' thresholds would leave the chart. Should the axis grow with the data?
  - In theory the simulations respond to changing the constants in `MainJFX`, but that has never been tried. Is that an intended way to experiment, and should it be tested?
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
