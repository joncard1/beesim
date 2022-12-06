package beesimulator.controller

/**
 * The [[beesimulator.ui.DataGenerator]] that the UI uses to query data from
 * the simulator. The initiative of the application comes from the UI, so the
 * data needs to be requested in reaction to user action, in this case either
 * starting the program or changing settings on the UI that require the data to
 * be recalculated. To ensure the dependencies go the proper direction, the
 * interface by which the UI requests data is defined by the UI, but implemented
 * here in the controller so that this application can supply the data that this
 * application wants displayed. It's not a very complex application, so perhaps
 * this is over-design, but that is the reason for this.
 */
package object generators {}
