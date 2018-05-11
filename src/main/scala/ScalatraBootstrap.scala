import javax.servlet.ServletContext
import org.scalatra._
import rest.NgsiLdWrapper

/**
  *
  *  Scalatra framework bootstrap class
  *
  *
  */
class ScalatraBootstrap extends LifeCycle with main.Configuration {
  override def init(context: ServletContext) {
    context.mount(new NgsiLdWrapper, "/*")

    context.initParameters(NgsiEndpoint) = System.getenv().getOrDefault(NgsiEndpoint,DefaultNgsiEndpoint)
    context.initParameters("org.scalatra.Port") = System.getenv().getOrDefault(Port,DefaultPort)
  }
}
