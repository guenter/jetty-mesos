package mesosphere.jetty_framework.scheduler

import org.apache.mesos.{SchedulerDriver, Scheduler}
import org.apache.mesos.Protos._
import java.util
import java.util.concurrent.atomic.AtomicBoolean
import java.nio.ByteBuffer
import java.util.logging.Logger
import scala.collection.JavaConverters._
import mesosphere.mesos.util.ScalarResource
import org.apache.mesos.Protos.Value.Ranges
import javax.inject.{Inject, Singleton}
import com.codahale.metrics.{Metric, MetricFilter, MetricRegistry}

/**
 * @author Tobi Knaup
 */

@Singleton
class JettyScheduler @Inject()(metricRegistry: MetricRegistry) extends Scheduler {

  val threshold = 10.0d
  val executorCmd = "java -cp /Users/tobi/code/jetty-mesos/target/jetty_framework-*-jar-with-dependencies.jar " +
    "mesosphere.jetty_framework.executor.Main"
  val endpoints = scala.collection.mutable.Set.empty[(String, Int)]
  val log = Logger.getLogger(getClass.getName)
  val metricNamePrefix = "throughput."


  def error(driver: SchedulerDriver, message: String) {}

  def executorLost(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, status: Int) {
    log.warning(s"Executor lost $executorId")
  }

  def slaveLost(driver: SchedulerDriver, slaveId: SlaveID) {}

  def disconnected(driver: SchedulerDriver) {}

  def frameworkMessage(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, data: Array[Byte]) {
    val diff = ByteBuffer.wrap(data).getLong
    log.info(s"${executorId.getValue} reported $diff new requests")
    val metricName = metricNamePrefix + executorId.getValue
    metricRegistry.meter(metricName).mark(diff)
  }

  def statusUpdate(driver: SchedulerDriver, status: TaskStatus) {
    log.info(s"Status update: $status")
  }

  def offerRescinded(driver: SchedulerDriver, offerId: OfferID) {}

  def resourceOffers(driver: SchedulerDriver, offers: util.List[Offer]) {
    for (offer <- offers.asScala) {
      if (shouldLaunch()) {
        val cpus = ScalarResource("cpus", 0.1)
        val taskId = "task_" + System.currentTimeMillis()
        val executorId = "exe_" + System.currentTimeMillis()

        val executor = ExecutorInfo.newBuilder
          .setExecutorId(ExecutorID.newBuilder.setValue(executorId))
          .setCommand(CommandInfo.newBuilder.setValue(executorCmd))

        val offeredPortsResource = offer.getResourcesList.asScala
          .find(r => r.getName == "ports" && r.getRanges.getRangeCount > 0)
          .get
        val port = offeredPortsResource.getRanges.getRange(0).getBegin

        val portsResource = offeredPortsResource.toBuilder
          .setRanges(Ranges.newBuilder.addRange(Value.Range.newBuilder.setBegin(port).setEnd(port).build))

        val task = TaskInfo.newBuilder
          .setName(taskId)
          .setTaskId(TaskID.newBuilder.setValue(taskId))
          .setExecutor(executor)
          .addResources(cpus.toProto)
          .addResources(portsResource)
          .setSlaveId(offer.getSlaveId)
          .build

        log.info(s"Launching $task")
        driver.launchTasks(offer.getId, List(task).asJava)
        endpoints.add((offer.getHostname, port.toInt))
      } else {
        driver.declineOffer(offer.getId)
      }
    }
  }

  def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo) {}

  def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo) {}

  private def shouldLaunch(): Boolean = {
    if (endpoints.isEmpty)
      return true

    val meters = metricRegistry.getMeters(new MetricFilter {
      def matches(name: String, metric: Metric): Boolean = name.startsWith(metricNamePrefix)
    })

    val mean = meters.asScala.values.map(_.getOneMinuteRate).sum / endpoints.size
    log.info(s"Mean throughput: $mean")
    mean > threshold
  }
}
