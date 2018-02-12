import com.azatprog.app._
import org.scalatra._
import javax.servlet.ServletContext

import com.mongodb.ServerAddress
import com.mongodb.casbah.Imports._




class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {


    val server = new ServerAddress("localhost", 27017)
    val mongoClient = MongoClient(server)
    val db = mongoClient("twit")
    context.mount(new MyScalatraServlet(db), "/*")
  }
}
