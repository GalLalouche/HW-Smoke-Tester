import com.github.gallalouche.MyScalatraServlet
import jakarta.servlet.ServletContext
import org.scalatra.LifeCycle

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext): Unit = {
    context.mount(new MyScalatraServlet, "/*")
  }
}
