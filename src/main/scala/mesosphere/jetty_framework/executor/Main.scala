package mesosphere.jetty_framework.executor

import org.apache.mesos.MesosExecutorDriver
import com.codahale.metrics.MetricRegistry


/**
 * @author Tobi Knaup
 */

object Main extends App {

  val registry = new MetricRegistry
  val executor = new JettyExecutor(registry)
  val driver = new MesosExecutorDriver(executor)
  val reporter = new MetricsReporter(registry, driver)

  driver.run()
}
