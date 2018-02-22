package com.azatprog.app

import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import org.scalatra._
// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

import authentikat.jwt.JwtHeader
import authentikat.jwt.JwtClaimsSet
import authentikat.jwt.JsonWebToken

case class Update(text: String = "")

case class Message(id: Int = 99, text: String = "Default text")

case class User(id: Option[Int], email: String, nickname: String, password: String)

case class UserCreds(nickname: String, password: String)

class MyScalatraServlet(db: MongoDB) extends ScalatraServlet with JacksonJsonSupport {

  protected implicit lazy val jsonFormats: Formats = DefaultFormats
  before() {
    contentType = formats("json")
  }

  var messages = List(Message(1, "Text 1"))

  private def genSalt() = java.util.UUID.randomUUID.toString

  private def genToken(nickname: String): (String, String) = {
    val header = JwtHeader("HS256")
    val claimsSet = JwtClaimsSet(Map("nickname" -> nickname, "createTime" -> System.currentTimeMillis() / 1000))
    val salt = genSalt()
    val jwt: String = JsonWebToken(header, claimsSet, salt)
    (jwt, salt)
  }

  private def isValidToken(jwt: String, salt: String): Boolean = {
    JsonWebToken.validate(jwt, salt)
  }

  post("/login") {
    val user = parsedBody.extract[UserCreds]
    val doc = db("users").findOne(MongoDBObject("nickname" -> user.nickname, "password" -> user.password))
    if (doc.isDefined) {
      val token = genToken(user.nickname)
      doc.get("salt") = token._2
      db("users").update(MongoDBObject("nickname" -> user.nickname), MongoDBObject("$set"-> MongoDBObject("salt"-> token._2)), false, true)
      MongoDBObject("code" -> 200, "token" -> token._1)
      //      new Object() {
      //        val code = 200
      //        val token = genToken(user.nickname)
      //      }
    } else {
      MongoDBObject("code" -> 401, "error" -> "User not found")
      //      new Object() {
      //        val code = 401
      //        val error = "User not found"
      //      }
    }
  }

  post("/register") {
    val newUser = parsedBody.extract[User]
    val user = db("users").find(MongoDBObject("email" -> newUser.email, "nickname" -> newUser.nickname))
    val isUserAdded = if (user.isEmpty) {
      db("users").insert(MongoDBObject("id" -> 1, "email" -> newUser.email,
        "password" -> newUser.password, "nickname" -> newUser.nickname)
      )
      true
    } else {
      false
    }
    isUserAdded
  }

  get("/") {
    //    views.html.hello()
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
    val msg = messages.find {
      _.id == id
    }
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
    messages = messages.filter {
      _.id != id
    }
    messages
  }

}
