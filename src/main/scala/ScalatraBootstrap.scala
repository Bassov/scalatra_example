import com.azatprog.app._
import org.scalatra._
import javax.servlet.ServletContext

import com.mongodb.ServerAddress
import com.mongodb.casbah.Imports._




class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {

    context.mount(new MyScalatraServlet(), "/*")
  }
}
