package com.azatprog.app

import models._
import org.scalatra._
// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._

import authentikat.jwt.JwtHeader
import authentikat.jwt.JwtClaimsSet
import authentikat.jwt.JsonWebToken

class MyScalatraServlet extends ScalatraServlet with JacksonJsonSupport {

  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  def response(data: Any = Map()) = Map("code" -> 200, "data" -> data)

  def response(code: Int, error: String) = Map("code" -> code, "error" -> error)

  before() {
    contentType = formats("json")
  }

  error {
    case HTTPException(code, msg) => response(code, msg)
    case e: Throwable =>
      response(500, e.getLocalizedMessage)
  }

  private var users = List(
    User(email = "dilyis@email.com", nickname = "dilyis", password = "xxx"),
    User(email = "mitya@email.com", nickname = "mitya", password = "xxx"),
    User(email = "azatprog@email.com", nickname = "azatprog", password = "xxx"),
  )
  private var tweets = List(
    Tweet(owner = users.head, text = "Hey"),
    Tweet(owner = users.head, text = "Ho"),
    Tweet(owner = users.head, text = "Let's go!")
  )

  private def genSalt() = java.util.UUID.randomUUID.toString

  private def genToken(nickname: String): (String, String) = {
    val header = JwtHeader("HS256")
    val claimsSet = JwtClaimsSet(Map("nickname" -> nickname, "createTime" -> System.currentTimeMillis() / 1000))
    val salt = genSalt()
    val jwt: String = JsonWebToken(header, claimsSet, salt)
    (jwt, salt)
  }

  def auth(): User = {
    val token = params.getOrElse("token", throw HTTPException(401, "Unauthorized, token parameter is missing"))
    val jwt = JsonWebToken.unapply(token).getOrElse(throw HTTPException(400, "Invalid token parameter"))
    val nickname = jwt._2.asSimpleMap.toOption
      .getOrElse(throw HTTPException(400, "Invalid token parameter"))
      .getOrElse("nickname", throw HTTPException(400, "Invalid token parameter"))
    val user = users.find(u => u.nickname == nickname).getOrElse(throw HTTPException(400, "Invalid token parameter"))
    if (!JsonWebToken.validate(token, user.salt))
      throw HTTPException(400, "Invalid token parameter")
    user
  }

  //
  // Tweets
  //
  // tweet the tweet
  post("/tweets") {
    val me = auth()
    val text = params.getOrElse("text", throw HTTPException(400, "Missing parameter text"))
    val tweet = new Tweet(owner = me, text = text)
    tweets = tweet :: tweets
    response(Tweet.map(tweet))
  }

  // get one tweet
  get("/tweets/:id") {
    val me = auth()
    try {
      val tweet = tweets.find(_.id == params("id").toInt).getOrElse(throw HTTPException(400, "Wrong format id"))
      response(Tweet.map(tweet))
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrond id format")
    }
  }

  // edit tweet
  put("/tweets/:id") {
    val me = auth()

  }

  // like tweet, dont foget to remove dislike
  put("/tweets/:id/like") {
    val me = auth()

  }

  // remove like from tweet
  delete("/tweets/:id/like") {
    val me = auth()

  }

  // dislike tweet, dont foget to remove like
  put("/tweets/:id/dislike") {
    val me = auth()

  }

  // remove dislike from tweet
  delete("/tweets/:id/dislike") {
    val me = auth()

  }

  // delete, dont forget to delete retweets
  delete("/tweets/:id") {
    val me = auth()

  }

  //retweet, auto mention of owner
  post("/tweets/:id/retweet") {
    val me = auth()
    val text = params.getOrElse("text", throw HTTPException(400, "Missing parameter text"))
    try {
      val origTweet = tweets
        .find(_.id == params("id").toInt)
        .getOrElse(throw HTTPException(400, "Wrong format id"))
      val tweet = new Tweet(
        owner = me, text = text,
        origTweet = Option(origTweet),
        mentioned = List(origTweet.owner)
      )
      tweets = tweet :: tweets
      response(Tweet.map(tweet))
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrond id format")
    }

  }

  //
  // FEED
  //
  get("/feed") {
    val me = auth()

  }

  //
  // USERS
  //
  get("/users/:id") {
    val me = auth()
    try {
      val user = users.find(_.id == params("id").toInt).getOrElse(throw HTTPException(400, "Wrong format id"))
      response(User.map(user))
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrond id format")
    }
  }

  // get user tweets
  get("/users/:id/tweets") {
    val me = auth()
    val userId = params.getOrElse("id", throw HTTPException(400, "Missing parameter id"))
    try {
      val user = users
        .find(_.id == userId.toInt)
        .getOrElse(throw HTTPException(400, "The user with such id is not found"))
      val userTweets = tweets.filter(t => t.owner.id == userId.toInt)
      response(userTweets.map(Tweet.map))
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrond id format")
    }
  }

  post("/users/:id/subscribe") {
    val me = auth()
    val userId = params.getOrElse("id", throw HTTPException(400, "Missing parameter id"))
    try {
      val user = users
        .find(_.id == userId.toInt)
        .getOrElse(throw HTTPException(400, "The user with such id is not found"))
      val subs = user :: me.subscriptions.filterNot(_ == user)
      users = me.copy(subscriptions = subs) :: users.filterNot(_ == me)
      response()
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrond id format")
    }
  }

  post("/users/:id/unsubscribe") {
    val me = auth()
    val userId = params.getOrElse("id", throw HTTPException(400, "Missing parameter id"))
    try {
      val user = users
        .find(_.id == userId.toInt)
        .getOrElse(throw HTTPException(400, "The user with such id is not found"))
      val subs = me.subscriptions.filterNot(_ == user)
      users = me.copy(subscriptions = subs) :: users.filterNot(_ == me)
      response()
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrond id format")
    }
  }

  //
  // AUTH
  //
  post("/register") {
    val nickname = params.getOrElse("nickname", throw HTTPException(400, "Missing parameter nickname"))
    val email = params.getOrElse("email", throw HTTPException(400, "Missing parameter email"))
    val password = params.getOrElse("password", throw HTTPException(400, "Missing parameter password"))
    if (users.exists(u => u.email == email || u.nickname == nickname)) {
      response(400, "User is already exists")
    } else {
      val (token, salt) = genToken(nickname)
      val newUser = User(email = email, nickname = nickname, password = password, salt = salt)
      users = newUser :: users
      response(Map("token" -> token))
    }
  }

  post("/login") {
    val nickname = params.getOrElse("nickname", throw HTTPException(400, "Missing parameter nickname"))
    val password = params.getOrElse("password", throw HTTPException(400, "Missing parameter password"))
    val user = users
      .find(u => u.nickname == nickname && u.password == password)
      .getOrElse(throw HTTPException(400, "User not exist or password incorrect"))

    val (token, salt) = genToken(user.nickname)
    users = user.copy(salt = salt) :: users.filterNot(u => u.nickname == user.nickname)
    response(Map("token" -> token))
  }

  post("/logout") {
    val me = auth()
    users = users.filterNot(u => u.nickname == me.nickname)
    users = me.copy(salt = genSalt()) :: users
    response()
  }
}
