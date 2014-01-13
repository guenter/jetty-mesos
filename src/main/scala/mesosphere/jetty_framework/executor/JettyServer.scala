package mesosphere.jetty_framework.executor

import com.codahale.metrics.MetricRegistry
import org.eclipse.jetty.server.handler.AbstractHandler
import org.eclipse.jetty.server.{Server, Request}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest}
import com.codahale.metrics.jetty8.InstrumentedHandler

/**
 * @author Tobi Knaup
 */

class JettyServer(val registry: MetricRegistry) {

  val helloHandler = new AbstractHandler {
    def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse) {
      response.setContentType("text/plain; charset=utf-8")
      response.setStatus(HttpServletResponse.SC_OK)
      baseRequest.setHandled(true)
      response.getWriter.println("Hello World")
    }
  }

  val instrumentedHandler = new InstrumentedHandler(registry, helloHandler)

  var server: Server = null

  def start(port: Int = 8080) = {
    server = new Server(port)
    server.setHandler(instrumentedHandler)
    server.start()
  }

  def stop() = server.stop()

}
