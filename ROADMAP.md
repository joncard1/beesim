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

## Laundry List

- Add labels to the bee set description charts. They seem to show up in "External temperature simulation", so maybe they are being hidden by the scroll bar, but I don't think so. (I think this is the legend of the chart, not a label and it may be good enough)
- It's possible I misunderstood the "colors drifting" issue described by Claude. I was referring to the colors in the bee set description charts do not match the colors of the corresponding line in the main chart. It's possible that the bee set description charts are in the wrong order and I can't tell because they aren't labelled. But that doesn't sound like the same issue described above, now that I read it again. Now that I run the application again, I'm seeing that the series in the main chart matches the color of the series in summary chart, but it doesn't match the colors in the legend.
- There is jitter in the sliders because of the varying width of the text representation of the value.
- I'd like the starting dimensions of the window to be different, but that may just need experimenting. Wider certainly, and probably taller, and the aspect ratio should be more like a screen or monitor ratio.
- Possibly some experimenting on changing the ranges, because the Heat In slider all the way at the top doesn't show behavior at the extreme of most populations. In theory, the system should respond dynamically to changing the constants in MainJFX, but I've never tried it.
- I don't remember exactly what I was trying to accomplish in some of the distributions, and I may not have made the same mistake in every experiment so it probably can't be a blanket change, but I think I calculated the distributions wrong because I didn't know about the Box-Muller Transform back then and I suspect I just attributed to the "spikes" the fact that the distributions didn't look bell-shaped, and they're actually not normally distributed. At least FocusedGenerator looks like it's doing it wrong; SingleFatherGenerator looks like it's linearly distributed (for good or ill), but MixedFathersGenerator doesn't make a lot of sense to me anymore. It appears to be attempting to stack multiple linear distributions; shouldn't that should look like a step-pyramid, if it was doing it right? BimodalGenerator is also doing it wrong. I suspect this should be a lot easier; shouldn't it just iterate over the number of bees and just generate a random number according to the correct distribution? I guess it would be less deterministic this way, but it would be simpler and I don't see why it would work much differently. Will it require sorting after generation? I can't remember if the step that calculates the histogram and CDF assume it's sorted.
- I never noticed before, but the summary charts have differently scaled y-axes. That's probably misleading.
- MainJFX isn't a good name for the name class.
- Add code coverage
- I am working off a checklist generated for another project for getting a project ready for general announcement. The list included:
    - LICENSE.md - The must be done by the author, not Claude
    - README.md - Should include more information such as known limitations, how to run the application, how to build the application. In this case, probably screen shots as part of the explanatory section at the top. The primary audience for the README is not developers; it's people interested in the subject matter, so the first section should address that: who should be reading it, what the simulation does, and what it is trying to show. But the other stuff should be included, too.
    - ROADMAP.md - Obviously, here, but I'd like to transition to using GitHub Issues for most things and ROADMAP for broader "vision" points and whole feature descriptions that have been committed to (nothing, really, for now).
    - CONTRIBUTING.md - I don't really know what to put into this kind of document. I'm not sure if that means allowing Claude to take a first crack at it, or to reserve that to myself.
    - GitHubActions CI - I'd like this to include code coverage report generation (report, don't cause failure for now), scalafmt (as annoying as scalafmt-driven failures are, I guess it should cause failures), and dependabot support (done) at minimum. I'm not sure what other static analysis tools are available, relevant, and simple enough to be worthwhile.
    - Issue and PR templates - In the other project, I ended up just with the defaults generated by GitHub, I think, and I haven't found them to be very helpful yet, but probably that's to be expected for single-developer projects. But I'd like there to be more developers, or at least people submitting bugs, so I assume these are needed. That project ended up with bug_report, feature_request, and question.
- Revisit question of whether project folder should be it .gitignore. I think it was created by Metals, and I excluded it thinking it was IDE specific and didn't belong in code control.