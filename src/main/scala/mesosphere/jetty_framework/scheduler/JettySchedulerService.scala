package mesosphere.jetty_framework.scheduler

import com.google.common.util.concurrent.AbstractExecutionThreadService
import mesosphere.mesos.util.FrameworkInfo
import org.apache.mesos.MesosSchedulerDriver
import javax.inject.{Named, Inject}

/**
 * @author Tobi Knaup
 */

class JettySchedulerService @Inject()(@Named("mesosMaster") val master: String,
                                      val scheduler: JettyScheduler) extends AbstractExecutionThreadService {

  val framework = FrameworkInfo("JettyMesos")
  val driver = new MesosSchedulerDriver(scheduler, framework.toProto, master)


  def run() {
    driver.run()
  }

  override def triggerShutdown() {
    driver.stop()
  }
}
