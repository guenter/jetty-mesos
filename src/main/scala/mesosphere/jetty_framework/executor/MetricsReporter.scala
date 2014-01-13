package mesosphere.jetty_framework.executor

import java.util.concurrent.{TimeUnit, Executors}
import com.codahale.metrics.MetricRegistry
import java.util.logging.{Logger, Level}
import java.nio.ByteBuffer
import org.apache.mesos.ExecutorDriver

/**
 * @author Tobi Knaup
 */

class MetricsReporter(val registry: MetricRegistry, val driver: ExecutorDriver) {

  val log = Logger.getLogger(getClass.getName)
  val scheduler = Executors.newScheduledThreadPool(1)
  val meterName = "mesosphere.jetty_framework.executor.JettyServer$$anon$1.requests"
  val period = 10
  var lastCount = 0L

  scheduler.scheduleAtFixedRate(new Runnable {
    def run() {
      try {
        val currentCount = registry.meter(meterName).getCount
        val diff = currentCount - lastCount
        lastCount = currentCount

        log.info(s"Reporting $diff new requests")
        val buf = ByteBuffer.allocate(8).putLong(diff).array
        driver.sendFrameworkMessage(buf)
      } catch {
        case t: Throwable => log.log(Level.WARNING, "argh", t)
      }
    }
  }, period, period, TimeUnit.SECONDS)
}
