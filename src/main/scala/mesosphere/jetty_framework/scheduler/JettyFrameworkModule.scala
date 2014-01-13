package mesosphere.jetty_framework.scheduler

import mesosphere.chaos.http.RestModule
import org.rogach.scallop.ScallopConf
import mesosphere.jetty_framework.http.EndpointsResource
import com.google.inject.Scopes
import com.google.inject.name.Names
import mesosphere.jetty_framework.AppConf

/**
 * @author Tobi Knaup
 */

class JettyFrameworkModule(val conf: ScallopConf with AppConf) extends RestModule {
  protected override def configureServlets() {
    super.configureServlets()

    bind(classOf[String])
      .annotatedWith(Names.named("mesosMaster"))
      .toInstance(conf.master())
    bind(classOf[EndpointsResource]).in(Scopes.SINGLETON)
  }
}
