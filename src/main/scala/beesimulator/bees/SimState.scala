package beesimulator.bees

/** A DTO object that holds the initialization variables for the simulator. The
  * mass and specific heat are needed to transform heat into temperature. The
  * temperature is the process variable (often written y(t)). The temperature in
  * a hive rises by the heat energy added to the it by the air, the sun, etc.,
  * and the temperature is a product of the total heat, the mass, and the
  * specific heat of the environment. To model a steady rate of heating, we need
  * to know how to convert heat into temperature. These are not real numbers; I
  * have no idea if any bee hive in creation can be described with these
  * numbers. For instance, a good rule of thumb is that the sun heats the Earth
  * at about 1kJ per m^2 every second. A heat flow of 0.1 J/step would either
  * imply the reaction time of these bees is truly amazing, or the hive is at
  * the bottom of the ocean.
  *
  * @param mass
  *   The mass of the hive
  * @param currentTemperature
  *   The current temperature in the hive
  * @param specificHeat
  *   The specific heat of the hive
  */
trait SimState {
  val mass: Double
  val specificHeat: Double
  val currentTemperature: Double
}

/** The state used by a simulator with bees that react to the internal
  * temperature of the hive.
  *
  * @param mass
  * @param specificHeat
  * @param currentTemperature
  */
final case class InternalSimState(
    mass: Double,
    specificHeat: Double,
    currentTemperature: Double
) extends SimState

/** The state used by a simulator with bees that react to the external
  * temperature of the hive. The internal temperature needs to be updated,
  * because the simulator's goal is to observe the internal temperature.
  *
  * @param mass
  * @param specificHeat
  * @param currentTemperature
  * @param externalTemperature
  * @param thermalConductivity
  */
final case class ExternalSimState(
    mass: Double,
    specificHeat: Double,
    currentTemperature: Double,
    externalTemperature: Double,
    thermalConductivity: Double
) extends SimState
