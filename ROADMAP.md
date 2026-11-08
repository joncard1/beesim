# Roadmap

Outstanding work and possible improvements, roughly in priority order within each section. Remove items as they are done.

## Needs a decision from the author

- **Spikes in the threshold histograms.** The generators give many bees exactly the same threshold (in "Mixed Fathers", 41 bees share 21.5°), and the bell-curve generators sample on a coarse grid, so some histogram bins spike. This is the populations themselves, not the display. Fixing it changes the populations the book demonstrates, for example by spreading daughters' thresholds continuously within each father's window instead of on a fixed grid. Tracked by the pending test "not have isolated spikes" in `EquilibriumSpec`.
- **Indicator for runs that never settle.** Some settings oscillate for all 250 steps (e.g. "Bimodal bee set" at heat in 1.0, heat per bee 0.002 swings between 20.42° and 20.79°), so the indicator, which shows the last step, jumps around while dragging. Options: average the last N steps, or show the range as a band.

## To verify

- **Colour drift between the temperature lines and the bee charts.** Reported by the author, but not reproduced headlessly: `MainControllerSpec` and ad hoc probes kept the correct colour classes through drags and simulation switches. Turning off the temperature chart's animation (`chart-fixes`) is the likely fix. If it still happens, note which elements change colour and when.

## Possible features

- **Show the equilibrium on the cumulative chart.** For simple bees, the settled state is where the cumulative distribution reaches heat in ÷ heat per bee. A horizontal line at that height would show the indicator crossing it, which is the core idea of the book.
- **UI interaction tests with TestFX.** TestFX's robot (with the existing Monocle setup) could drag the sliders and test the conflation behaviour and colours end to end.

## Performance

A full internal-simulation run takes about 50 ms after the `simulation-performance` work (measure with `sbt "Test/runMain beesimulator.SimulationBenchmark"`). Each update is now bound by the slowest single generator. If more speed is needed:

- Stop a simulation early once it has settled and fill the rest of the series with the settled value (doesn't help oscillating runs).
- Run fewer steps while a slider is being dragged.
- The "abort runs older than one second" rule discussed for `ConflatingRunner` is probably unnecessary at this speed.

Already tried without measurable gain: collecting bees into array buffers instead of lists in `Sim.executeStep`. Profiling shows the time is in the bees' `join`/`leave`/`update` calls, not in collection building, so memory layout of the bees isn't the bottleneck.

## Cleanup

- **Unused dependencies** in `build.sbt`: `rxscala_2.13`, `scala-swing`, `scala-parser-combinators`, `scalamock_2.13` and `scalacheck-1-16` aren't imported anywhere (check before removing). `rxscala` and `scalamock` are Scala 2.13 artifacts. ScalaTest 3.2.11 is also old.
- **Dependabot.** GitHub still reported vulnerabilities on push after the JavaFX 25 upgrade; check which alerts remain.
- **README build instructions** predate the JavaFX 25 upgrade: they mention providing JavaFX in a `lib` folder, and don't mention that a recent JDK (25 is used locally) is now required. The README and package Scaladoc also have citation placeholders ("find the citation", "LOOKUP").
- **`ExternalBeeSimSpec`** contains only a placeholder test ("NOT SURE WHAT THE TEST SHOULD BE"). `EquilibriumSpec` covers only the internal simulation.
- **`Sim.toString`** interpolates the method `newState` rather than `currentState`, so it prints a function value.
- **Error handling.** `ConflatingRunner` failures fall back to printing a stack trace; `MainController` shows nothing to the user. There is also an old TODO in `MainController` about being more defensive toward user-written bees and generators.

## Watch

- **Monocle** is published only up to 21.0.2 but works with JavaFX 25. A JavaFX upgrade could break it; the UI tests would then fail at startup. A future JavaFX release may include a built-in headless platform that replaces it (not in 25.0.2).
