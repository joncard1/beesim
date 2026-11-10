# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

A ScalaFX desktop app that simulates honeybee "bearding" (hive temperature regulation) as emergent behavior: each bee has its own temperature threshold, and the hive temperature stabilizes based on the distribution of those thresholds across the population. Numeric constants are tuned to look like realistic Celsius values, not taken from real data.

## Commands

Scala 3.10.0, sbt 1.12.9 (pinned in `project/build.properties`). CI (`.github/workflows/scala.yml`) runs `sbt test` on JDK 25.

```sh
sbt compile
sbt test
sbt scalafmtAll   # format the code; CI fails if it isn't formatted (scalafmtCheckAll)
sbt "testOnly beesimulator.bees.InternalBeeSimSpec"
sbt "testOnly beesimulator.bees.InternalBeeSimSpec -- -z \"stabilize at 2 bees\""   # one test by name
sbt run        # launches the GUI (beesimulator.BeeSimulatorApp)
sbt "Test/runMain beesimulator.SimulationBenchmark"   # times a warmed-up run of each simulation
sbt coverage test coverageReport   # HTML report in target/**/scoverage-report/index.html
```

Tests use ScalaTest `AnyWordSpec` with `Matchers`. UI tests run headlessly through Monocle (`org.testfx:openjfx-monocle`, test scope): `src/test/scala/beesimulator/ui/JavaFxToolkit.scala` starts JavaFX with the headless properties, and `onFxThread` runs code on the JavaFX thread. Monocle is published only up to 21.0.2 but works with JavaFX 25; if a JavaFX upgrade breaks it, the UI tests fail at startup. Known bugs are recorded as `pendingUntilFixed` tests; remove the wrapper when fixing one. The README says the JavaFX libraries may have to be provided externally (the author keeps them in an untracked `lib/` folder). `project/build.properties` and plugin `.sbt` files in `project/` are tracked; Metals' generated `project/metals.sbt` and `project/project/` are ignored.

## Architecture

The code has four layers, and they depend on each other in this order:

**`simulator/`: generic step engine.** `Sim[T, U, V]` (T = forcing value type, U = state, V = actor) runs one step in `executeStep`. Each active actor is asked whether it wants to `leave`, then each inactive actor whether it wants to `join`. The subclass's `newState(forcing1, forcing2, inactiveCount, activeCount, state)` computes the next state, and `SimActorProvider.update` gives each actor a chance to update its memory. The simulation is immutable: `step` (from the `SimOps` implicit class, which needs an implicit `SimProvider` in scope and `import sim._`) returns a new `Sim` built by `SimProvider.createSim`. Every simulation has exactly two forcing variables.

**`bees/`: domain implementation.** `Bee[-T]` extends `SimActor` and adds a `threshold`, which the UI histograms use. There are two simulation variants, each with its own state case class and sim:
- `InternalBeeSim` / `InternalSimState`: bees sense the hive temperature. The forcing variables are heat in and heat removed per bee.
- `ExternalBeeSim` / `ExternalSimState`: bees sense the external temperature, and heat moves through the hive walls by thermal conductivity.

Bee variants (`SimpleBee`, `DerivativeBee`, `IntegralBee`, `CombinedIntegralBee`, `SimpleExternalBee`) are case classes. Their per-step state update is **not** defined on the bee; it lives in the pattern match of the implicit `SimActorProvider` objects in `object Bee` (`Bee.scala`). When you add a bee type, you must add a case there, or the match fails at runtime.

**`controller/`: population setup.** `InternalSimulationInitializer` and `ExternalSimulationInitializer` build a `ui.Simulation`: forcing variable names, ranges, and defaults, plus an `AggregateDataGenerator` holding several population generators, which it runs in parallel. The generators in `controller/generators/` (MixedFathers, SingleFather, Bimodal, Focused) extend `AbstractDataGenerator`, which owns the iteration loop (`runSimulator`). Concrete generators only decide how bee thresholds are distributed (each builds its population once, in a lazy val), usually as "fathers" (genetic sub-populations) each with many "daughters". Many generators take a factory such as `DerivativeBee.apply(_, ...)` to pick the bee type. There is no dependency injection: to add a population profile, write a generator and register it in an initializer.

**`ui/`: ScalaFX/JavaFX front end.** `BeeSimulatorApp` creates both simulations, loads `src/main/resources/beesimulator/ui/beecharts.fxml`, and passes the simulations to `MainController`. FXML injects JavaFX delegate fields, which are wrapped in `lazy` ScalaFX objects (the reasoning is documented at the top of `MainController`). When a slider changes, `MainController` submits the slider values to a `ConflatingRunner`, which runs `DataGenerator.generatePoints` on a background thread one run at a time, skipping slider positions passed over while busy, and delivers the `Dataset`s on the JavaFX thread. A `Dataset` holds the temperature series plus the bee thresholds. `ui/processors/` turns those into histogram or cumulative-frequency chart data.

`src/main/scala/beesimulator/package.scala` contains a long package-level Scaladoc explaining the biological background and the design intent.

Work items are GitHub issues (`gh issue list`). Issues labelled `author-task` are reserved for the author: don't work on them. `ROADMAP.md` holds longer-term direction, open questions that aren't ready to be issues, and notes such as performance approaches already tried.
