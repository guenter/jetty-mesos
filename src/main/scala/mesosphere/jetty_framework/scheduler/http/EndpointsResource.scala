package mesosphere.jetty_framework.http

import javax.ws.rs.{GET, Path}
import javax.inject.Inject
import mesosphere.jetty_framework.scheduler.JettyScheduler

/**
 * @author Tobi Knaup
 */

@Path("v1/endpoints")
class EndpointsResource @Inject()(val scheduler: JettyScheduler) {

  val globalPort = 9000

  @GET
  def endpoints() = {
    val endpoints = scheduler.endpoints.map(e => s"${e._1}:${e._2}").mkString(" ")
    s"Jetty_$globalPort $globalPort $endpoints\n"
  }
}
