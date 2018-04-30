import javax.servlet.ServletContext
import org.scalatra._
import server.NgsiLdWrapper

/**
  *
  *  Scalatra framework bootstrap class
  *
  *
  */
class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context.mount(new NgsiLdWrapper, "/*")
    Console.println("Here!!!!!!")
  }
}
