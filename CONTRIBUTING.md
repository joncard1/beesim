# Contributing

Thanks for your interest in the bee bearding simulator. Bug reports, questions and ideas are as welcome as code.

## Reporting bugs and asking questions

Open an [issue](https://github.com/joncard1/beesim/issues/new/choose) and pick the bug report, feature request or question form. For anything you can see on screen, a screenshot and the slider positions help a lot.

## Building and running

You need a recent JDK (25 is used for development and CI) and [sbt](https://www.scala-sbt.org/). sbt downloads Scala, ScalaFX and JavaFX itself.

```sh
sbt run      # open the simulator
sbt test     # run all tests
sbt "testOnly beesimulator.bees.InternalBeeSimSpec"   # run one test class
```

The UI tests run JavaFX headlessly through Monocle, so `sbt test` doesn't open any windows and works on machines without a display, including CI.

## Making a change

1. For anything bigger than a small fix, open or comment on an issue first, so we can agree on the approach.
2. Create a branch from `master` for the change. Keep each pull request to one change; separate improvements go in separate pull requests.
3. Add or update tests. The simulation logic is tested directly, and the UI with headless tests in `src/test/scala/beesimulator/ui`.
4. Run `sbt test` and make sure it passes.
5. Open a pull request against `master`, fill in the template, and link the issue with "Fixes #N".

### Conventions

- **Code style:** match the surrounding code, including its Scaladoc comments.
- **Known bugs:** a test for a bug that isn't fixed yet can be written now and wrapped in ScalaTest's `pendingUntilFixed`. It's reported as pending until the bug is fixed, then fails as a reminder to remove the wrapper.
- **New bee populations:** write a generator in `src/main/scala/beesimulator/controller/generators/` and register it in `InternalSimulationInitializer` or `ExternalSimulationInitializer`.
- **New kinds of bee:** each bee type's per-step update lives in the pattern match in `object Bee` (`Bee.scala`); a new type needs a case there.

`CLAUDE.md` has a fuller description of the architecture.

## License

The project's license hasn't been chosen yet.
