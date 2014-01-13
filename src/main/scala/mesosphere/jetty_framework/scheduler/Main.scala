package mesosphere.jetty_framework.scheduler

import mesosphere.chaos.http.{HttpService, HttpConf, HttpModule}
import org.rogach.scallop.ScallopConf
import mesosphere.chaos.AppConfiguration
import mesosphere.chaos.metrics.MetricsModule
import mesosphere.jetty_framework.AppConf

/**
 * @author Tobi Knaup
 */

object Main extends mesosphere.chaos.App {

  // Declare all Guice Modules
  def modules() = {
    Seq(
      new HttpModule(conf),
      new MetricsModule,
      new JettyFrameworkModule(conf)
    )
  }

  //The fact that this is lazy, allows us to pass it to a Module
  //constructor.
  lazy val conf = new ScallopConf(args)
    with HttpConf with AppConfiguration with AppConf

  run(Seq(
    classOf[HttpService],
    classOf[JettySchedulerService]
  ))
}
