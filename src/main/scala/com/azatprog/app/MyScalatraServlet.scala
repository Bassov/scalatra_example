package com.azatprog.app

import org.scalatra._
// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._
import javafx.css.ParsedValue

case class Update(text: String="")
case class Message(id: Int=99, text:String="Default text")

class MyScalatraServlet() extends ScalatraServlet with JacksonJsonSupport {

  protected implicit lazy val jsonFormats: Formats = DefaultFormats
  before() {
    contentType = formats("json")
  }
  
  var messages = List(Message(1, "Text 1"))

  get("/") {
    views.html.hello()
  }

  get("/messages") {
    messages
  }
  
  post("/messages") {
    val msg = parsedBody.extract[Message]
    messages = msg :: messages
    messages
  }
  
  def getParamId = params.getOrElse("id", halt(400)).toInt
  def checkExistence(id: Int) = if (!messages.exists(_.id == id))
      halt(404)
  
  get("/messages/:id") {
    val id = getParamId
    checkExistence(id)
    val msg = messages.find { _.id == id }
    msg.get
  }
  
  put("/messages/:id") {
    val id = getParamId
    checkExistence(id)
    val text = parsedBody.extract[Update].text
    val msgUpdate = Message(id, text)
    for ((msg, inx) <- messages.zipWithIndex) {
      if (msg.id == id) {
        messages = messages.updated(inx, msgUpdate)    
      }
    }
    messages
  }
  
  delete("/messages/:id") {
    val id = getParamId
    checkExistence(id)  
    messages = messages.filter { _.id != id }
    messages
  }

}
