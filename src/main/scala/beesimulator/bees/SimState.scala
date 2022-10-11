package beesimulator.bees

/**
  * A DTO object that holds the initialization variables for the simulator. The
  * mass and specific heat are needed to transform heat into temperature. The
  * temperature is the process variable (often written y(t)), and the heat flow
  * is the control variable (often written u(t)). The temperature in a hive
  * rises by the heat energy added to the it by the air, the sun, etc., and the
  * temperature is a product of the total heat, the mass, and the specific heat
  * of the environment. To model a steady rate of heating, we need to know how
  * to convert heat into temperature. These are not real numbers; I have no idea
  * if any bee hive in creation can be described with these numbers. For
  * instance, a good rule of thumb is that the sun heats the Earth at about 1kJ
  * per m^2 every second. A heat flow of 0.1 J/step would either imply the
  * reaction time of these bees is truly amazing, or the hive is at the bottom
  * of the ocean.
  *
  * @param mass                 The mass of the hive
  * @param currentTemperature   The initial temperature in the hive
  * @param specificHeat         The specific heat of the hive
  * @param heatFlowPerBee       The amount of heat flow each active bee is contributing
  */
final case class SimState(mass: Double, currentTemperature: Double, specificHeat: Double, heatFlowPerBee: Double)
