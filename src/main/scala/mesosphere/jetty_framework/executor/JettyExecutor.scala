package mesosphere.jetty_framework.executor

import org.apache.mesos.{ExecutorDriver, Executor}
import org.apache.mesos.Protos._
import com.codahale.metrics.MetricRegistry
import java.util.logging.Logger
import scala.collection.JavaConverters._

/**
 * @author Tobi Knaup
 */

class JettyExecutor(val registry: MetricRegistry) extends Executor {

  val server = new JettyServer(registry)
  val log = Logger.getLogger(getClass.getName)

  def error(driver: ExecutorDriver, message: String) {}

  def shutdown(driver: ExecutorDriver) {
    log.info("Shutting down")
    server.stop()
  }

  def frameworkMessage(driver: ExecutorDriver, data: Array[Byte]) {
    log.info(s"Framework message ${data.length}")
  }

  def killTask(driver: ExecutorDriver, taskId: TaskID) {
    log.info("Stopping the server")
    server.stop()

    val status = TaskStatus.newBuilder
      .setTaskId(taskId)
      .setState(TaskState.TASK_FINISHED)
      .build
    driver.sendStatusUpdate(status)
  }

  def launchTask(driver: ExecutorDriver, task: TaskInfo) {
    val port = task.getResourcesList.asScala
      .find(r => r.getName == "ports" && r.getRanges.getRangeCount > 0)
      .map(r => r.getRanges.getRange(0).getBegin.toInt)
      .getOrElse(8080)

    log.info(s"Starting the server on port $port")
    server.start(port)

    val status = TaskStatus.newBuilder
      .setTaskId(task.getTaskId)
      .setState(TaskState.TASK_RUNNING)
      .build
    driver.sendStatusUpdate(status)
  }

  def disconnected(driver: ExecutorDriver) {}

  def reregistered(driver: ExecutorDriver, slaveInfo: SlaveInfo) {}

  def registered(driver: ExecutorDriver, executorInfo: ExecutorInfo, frameworkInfo: FrameworkInfo, slaveInfo: SlaveInfo) {
    log.info(s"Registered to slave $slaveInfo")
  }
}
