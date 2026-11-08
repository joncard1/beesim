/**
  * This application was created to demonstrate various strategies by bees while
  * "bearding". It attempts to demonstrate the concepts of emergent behavior in
  * a well-known instance of bees engaging in coordinated action that is none-
  * the-less not centrally organized or directed, with no coercion by higher-
  * status bees.
  * 
  * "Bearding" is the practice by bees of regulating the temperature of the hive
  * by large groups of bees gathering at the entrance to the hive and
  * circulating air through the hive by fanning their wings. The temperature in
  * a hive is highly-regulated, with a required temperature between LOOKUP and
  * LOOKUP, with an optimum at 35 degrees Celsius. Any thing else could cause
  * the developing brood to be harmed. This is achieved, but it apparently was
  * achieved without obvious coordination from a central authority and it was
  * not clear how the bees knew which bees should joing the bearding and which
  * should not until a paper in lOOKUP by LOOKUP AND LOOKUP, who observed that
  * the bees that joined the bearding at different temperatures were grouped by
  * the genetics of their father (they all share the same mother). It appeared
  * that there were different temperature preferences among the bees that
  * triggered the joining of the task that were determined by genetics.
  * 
  * The main functionality of the simulator is in the package
  * [[beesimulator.controller]], which contains the logic of the simulator. This
  * system utilizes a variant of the concept of a PID controller, which is a
  * system that uses sensor feedback from the results of a control variable to
  * vary the control variable to achieve a targeted sensor value, and it does
  * this using three strategies that are combined together: considering the
  * degree to which the sensor differs from the desired control point, the past
  * difficulty in achieving the control point, and the speed at which the
  * variation from the control point is getting better or worse. These three
  * terms are called the Proportional term, the Integral term, and the
  * Differential term. The standard expression of the PID equation is: u(t) =
  * Kp * e(t) + Kp/Ti * (&#8747; e(&tau;)d&tau; where 0 < &tau; < t) +
  * Td * Kp * d(e(t))/dt where u(t) is the control variable to the send to the
  * system being controlled, e(t) is the difference between the current sensor
  * and the desired control point, usually g(t) - c where g(t) is the raw sensor
  * value and c is the desired value. However, the typical PID controller is
  * implemented differently here, because there may be no value of c in the
  * system, as the different bees may not have a shared understanding of the
  * desired temperature of the hive. Because of the insight of LOOKUP AND
  * LOOKUP, we can substitute (g(t) - c) = e(t) for b(g(t)), meaning the number
  * of bees that think a particular temperature is "too hot". The distribution
  * of these preferences across the bee population by inheritance from different
  * fathers causes the function b to vary with temperature just as the function
  * e does, and this can be used to create a P controller and a PD controller,
  * that is to say, a proportion-only controller and a proportion and
  * differential controller. However, I have not been able to come up with an
  * implementation that can achieve the features in a PI or PID controller,
  * namely getting rid of "steady state error". Steady state error happens when
  * there is an outside force preventing the system from achieving equilibrium
  * and the PID controller must exert an equivalent non-zero value for u(t) to
  * oppose that in order to keep equilibrium at the desired point. Without an I
  * system, the system can only reach equilibrium when the P term contains
  * enough error to register a non-zero value for u(t) that equals the outside
  * force. The D term can hold because a differential does not have a relation
  * to any absolute values, and the P term can work because the purpose of the
  * term c is so that e mirrors g, but takes the control point as 0. If we
  * consider b(g(t)) as &#x03A3;(bi &#x2208; Bee thresholds | bi < g(t): 1),
  * then this function is also rooted with at 0 and is dependent on t. Unlike
  * e(t), b(g(t)) cannot be negative, but if we assume that the minimum
  * temperature threshold at which at least 1 bee in the hive joins a bearding
  * is below the critical temperature (35 degrees, in this case) and the heat
  * influx from outside is always acting to raise the temperture, then we only
  * need consider the case where b(g(t)) is positive. If the outside temperature
  * is cold enough to be drawing heat out of the hive, we have to assume there
  * is a different mechanism and the bees can tell the difference. But the I
  * term must be in terms of c, because the integral of e(t) is necessarily in
  * relation to that specific 0, and cannot be adjusted to a different unit,
  * like we do with exchanging e(t) for b(g(t)). Strictly, it is not necessary
  * for the term to utilize an integral, but we need some mechanism of
  * generating a value proportional to the outside force, and it is difficult to
  * imagine a means of deriving the size of the outside force without reference
  * to some base condition where an outside force is not present.
  * 
  * The class [[beesimulator.simulator.Sim]] contains the simulator, which
  * steps through time, testing the temperature in the "hive" against all of the
  * "bees" in the hive, giving them a chance to join or leave the bearding, and
  * then calculating the amount of heat a bearding of that size would expel vs
  * the influx of heat provided at the start of the simulation. The logic for
  * joining the bearding is kept separate from the simulator to preserve the 
  * emergent nature of the problem in the different implementations of
  * [[beesimulator.bees.Bee]]. A [[beesimulator.bees.SimpleBee]] is a simple P
  * controller, where each bee either joins or leaves the bearding based on a
  * simple threshold. The sum of all such decisions provides the basis of the
  * function b. A [[beesimulator.bees.DerivativeBee]] implements a PD
  * controller, comparing the provided temperature to the prior temperature to
  * introduce a D component. The [[beesimulator.bees.IntegralBee]] is an attempt
  * to integrate an I controller; it provides some of the features of an I
  * controller, namely it removes a lot of instability and noise while achieving
  * equilibrium, but it does not correct for steady state error.
  * 
  * The [[beesimulator.ui]] system requests data to be recalculated at various
  * times, and it provides the trait [[beesimulator.ui.DataGenerator]] to
  * provide the means to do this. There are various implementations of this
  * depending on how the population is generated, including a
  * [[beesimulator.controller.generators.AggregateDataGenerator]] for tying all of the
  * implementations together to provide to the UI a single generator.
  * 
  * The package [[beesimulator.ui]] contains the logic for displaying the data
  * generated by the simulator, and while I have made notes on things like
  * design choices, is not really useful for the purposes of the simulator or
  * the accompanying literature.
  */
object beesimulator {}
