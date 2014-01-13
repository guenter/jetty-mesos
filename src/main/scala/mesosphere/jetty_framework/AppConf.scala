package mesosphere.jetty_framework

import org.rogach.scallop.ScallopConf

/**
 * @author Tobi Knaup
 */

trait AppConf extends ScallopConf {

  lazy val master = opt[String]("master",
    descr = "The Mesos masters",
    required = true,
    noshort = true)
}
