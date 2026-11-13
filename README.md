# Bee Bearding Simulator

How does a honeybee colony keep its hive at the right temperature when no bee is in charge? This simulator lets you explore one answer: every bee follows its own simple rule, and the colony's response comes from how those rules differ from bee to bee.

It accompanies **Eusocial Cooperation**, an as-yet-unpublished book by me (Jonathan Card), and is written for readers interested in emergent behavior, how human civilizations resemble insect hives, and how decentralized systems can regulate themselves. You don't need to be a programmer to use it.

![The simulator showing the internal temperature simulation, with a histogram of each population's temperature thresholds above a chart of the hive temperature over time](docs/images/histogram.png)

## What it simulates

When a hive gets too warm, groups of bees gather at the entrance and fan their wings to move air through it, a behavior called *bearding*. The colony keeps the brood at a closely regulated temperature this way, yet no bee directs the others.

Research on honeybees (Jones et al., 2004) found that the bees that join in at different temperatures are grouped by the genetics of their fathers (all the workers share the same mother). Each bee seems to have its own temperature threshold for taking up the task. In the literature this is known as the *response threshold* model.

The simulator models exactly that:

- Each bee has a threshold. When the temperature rises above it, the bee joins the bearding; when it falls below, the bee leaves.
- Each active bee removes a little heat; heat also flows into the hive.
- Step by step, the temperature changes with the balance between the two, and each bee decides again.

Different populations of bees are built in, with their thresholds spread in different ways: daughters of many fathers mixed together, a single father, two or several clusters, and bees that also react to how fast the temperature is changing or to how long it has been too warm.
There are some assumptions that don't necessarily match reality; my interest is in examining the algorithm, not actual bees. The most important possibly-silly assumption is that the daughters of a particular father will have temperature thresholds linearly centered around the father's threshold. Like I said, this may be silly, but even in this case the more fathers there are the more the distribution of the entire hive will become normally distributed. You can start to see that in the populations labelled "Multiple Fathers"; these are multiple linear distributions layered on top of each other, and the populations are starting to look more bell-shaped already. Some of the population shapes are basically chosen creatively; there's every chance that such populations can't exist in reality.

There are two simulations:

- **Internal temperature:** bees react to the temperature inside the hive, and heat flows in at a rate you set.
- **External temperature:** bees react to the temperature outside the hive, and heat flows in through the hive walls according to the difference between the outside and inside temperatures.

## What it shows

Without any bee knowing the "right" temperature, the colony settles at a stable temperature. The more heat flows in, the more bees it takes to balance it, and the higher the temperature at which enough of them have joined. The shape of the population's thresholds decides how the colony responds.

In control-engineering terms, this works like a proportional (P) controller, or a proportional-derivative (PD) controller with the bees that react to the rate of change. These leave a *steady-state error*: the hive settles at a temperature that depends on how much heat is coming in. Removing that error normally needs an integral term, which in turn needs a shared idea of the target temperature. Without shared knowledge of an "optimal" temperature, there is no way for individual bees to decide whether to keep bearding below their own threshold to counter the outside pressure.
By adjusting the Heat In slider and the Heat Per Bee slider, you can observe the different ways the system stabilizes. By increasing the heat, the stable temperature of the hive drifts upwards, but at different rates. This rate is the equivalent of the *steady-state error* described above. The rate at which the stable temperature changes compared to the Heat In slider changes is based on the slope of the Cumulative Distribution Function (CDF) of the probability distribution of the thresholds (which is the same as the height of the Probability Density Function (PDF). The PDF and the CDF are equations that correspond to the Histogram and Cumulative view of the bee populations in the UI, although strictly speaking the UI is based on the bee populations used in the simulation, not plots of the equations themselves). Populations with spikes in the thresholds (like the one labelled "Bimodal bee set") will see the stable temperature shoot upwards when the heat flow increases beyond a certain point, whereas the linear distributions like "Single Father with medium thresholds" and "Single Father with low thresholds" increase at the same constant rate, but the population with low thresholds stabilizes at a lower temperature.

The populations labelled "Mixed Fathers with improved integrals" is a cheat. It manages to eliminate the steady-state error and maintain stability, but it relies on the population (descended from many fathers, all of whom are probably from different hives from each other, etc.) all inheriting a shared knowledge of what the "optimal" temperature is for a hive, but they also inherit different thresholds for when they join the bearding or not. This may be true; humans all have a universally shared "knowledge" of what our body temperatures should be (although I think our internal body temperature isn't universally 98.6 F, and there's more variation in that than would be usable in a bee hive, which is +/- 0.5 C). It would require the bees to use a different value for the hive been "too hot" than they use for "I should do something about it being too hot" (believable) and for evolution to have produced a species-wide very narrow diversity in the "too hot" value, and to have specifically evolved wider probability distributions in the values of the task activation value, which would be fascinating to know if that's possible. On first blush, that would go in the face of the "Selfish Gene" approach made famous by Richard Dawkins in in favor of "group selection" often seen as championed by the late E. O. Wilson, but I'm not sure it necessarily would given the difference in the complexity of haploid genetics (as opposed to diploid genetics we are usually taught in school) seen in eusocial insects and the differences in the way the "cost function" is evaluated in this case (the hive suffers a cost for having the wrong probability distribution, but individuals don't suffer much cost having any particular threshold; it's just a matter of doing one thing instead of another and then they don't reproduce themselves, anyway).

The simulation named "External Temperature Simulation" relies on the bees responding to the temperature outside the hive rather than the temperature inside the hive. This approach did eliminate steady-state error, but it relied on the bees responding to the outside temperature with evolutionary pressure tuning the variables rather than negative feedback, so it may not be stable. If the distribution of the bees' thresholds matches other variables, such as the rate at which heat flows through the hive walls, then the graduated response of the population can match the change in heat flow caused by the difference between the outside and inside temperatures. This shows that a solution to steady-state error may exist from emergent behavior, but the data from real hives that I have seen is too noisy to tell whether real hives have steady-state error or not.

## Reading the screen

- **Population charts (top):** one per population, colored to match its line in the temperature chart. Each summarizes the bees' thresholds, either as a **histogram** (how many bees have a threshold in each small temperature range) or as a **cumulative** distribution (how many bees have a threshold below each temperature). Scroll sideways to see them all. All the charts share the same vertical scale, so they can be compared.
- **Indicator:** the vertical line on each population chart marks where its simulation ended up: the final hive temperature (internal simulation) or the outside temperature (external simulation). For the populations of simple bees, the height where the line crosses the cumulative curve is the number of bees bearding.
- **Band:** when a simulation never settles and keeps swinging up and down, a shaded band around the indicator shows the range of temperatures it was still swinging over at the end.
- **Temperature chart (bottom):** the hive temperature over time for every population.
- **Sliders (right):** the heat flowing in (or, in the external simulation, the outside temperature) and the heat each bearding bee removes. Drag them and watch the charts follow.
- **Simulation and Summary type (top left):** switch between the two simulations and between histogram and cumulative charts.

![Cumulative charts for the internal simulation, where one population never settles: its temperature keeps swinging, and the band on its chart shows the range](docs/images/cumulative.png)

![The external temperature simulation, where the indicator marks the outside temperature](docs/images/external.png)

## Running the simulator

There is no formally packaged download yet, so the simulator is run from its source code (you may be able to get jar file from the most recent merge into master):

1. Install a recent Java Development Kit (JDK 25 is used for development), for example from [Adoptium](https://adoptium.net/).
2. Install [sbt](https://www.scala-sbt.org/download/), the Scala build tool.
3. Download or clone this repository, open a terminal in its folder, and run:

   ```sh
   sbt run
   ```

   The first run downloads Scala, ScalaFX and JavaFX, which takes a few minutes.

## Known limitations

- **The numbers are illustrative, not measurements.** The constants are chosen to give realistic-looking temperatures in degrees Celsius from realistic-looking heat inputs, but the population size was chosen to be a round number, and values such as the hive's specific heat and each bee's cooling ability were chosen to fit. The simulator shows that emergent behavior *can* regulate temperature this way; it doesn't model a particular real hive.
- **The populations are built in.** New populations can be added by a programmer (see [CONTRIBUTING.md](CONTRIBUTING.md)), but not from within the application.
- **The slider ranges don't reach every population's extremes.** At the top of the heat slider, most populations still haven't reached their limits, and the temperature chart's scale is fixed.
- **It has to be built from source** and needs a recent JDK. (A packaged jar file may be found in the most recent merge to master, but we don't have formal releases with fixed milestones and guaranteed stability)

## For developers

```sh
sbt test    # run all tests; the UI tests run headlessly, without opening windows
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for how to contribute and `CLAUDE.md` for an overview of the code. The screenshots above are rendered from the real application, headlessly, with:

```sh
sbt "Test/runMain beesimulator.ui.ReadmeScreenshots docs/images"
```

## References

Jones, Julia C., Mary R. Myerscough, Sonia Graham, and Benjamin P. Oldroyd. 2004. "Honey Bee Nest Thermoregulation: Diversity Promotes Stability." *Science* 305: 402–404. <https://doi.org/10.1126/science.1096340>

## License

This project is licensed under the [Apache License 2.0](LICENSE). Unless you say otherwise, any contribution you submit for inclusion is licensed under the same terms, as described in section 5 of the license.
