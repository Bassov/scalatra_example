package com.azatprog.app

import java.util.Date

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

  before() {
    contentType = formats("json")
  }

  private var users = List[User]()
  private var tweets = List[Tweet]()

  private def genSalt() = java.util.UUID.randomUUID.toString

  private def genToken(nickname: String): (String, String) = {
    val header = JwtHeader("HS256")
    val claimsSet = JwtClaimsSet(Map("nickname" -> nickname, "createTime" -> System.currentTimeMillis() / 1000))
    val salt = genSalt()
    val jwt: String = JsonWebToken(header, claimsSet, salt)
    (jwt, salt)
  }

  //
  // Tweets
  //
  // tweet the tweet
  post("/tweets") {

    // TODO verification by token
    val tweetForm = parsedBody.extract[TweetForm]

    val owner = UserData.getUserById(tweetForm.owner)
    if (owner.isEmpty) {
      halt(505)
    }

    var origTweet = Option[Tweet](null)
    var mentioned = List[User]()
    // TODO parse text to know how was mentioned in tweet and add to mentioned
    if (tweetForm.origTweet.isDefined) {
      origTweet = TweetData.getTweetById(tweetForm.origTweet.get)
      if (origTweet.isDefined) {
        mentioned = origTweet.get.owner :: mentioned
      }
    }
    val tweet = Tweet(1, owner.get, new Date(), tweetForm.text,
      mentioned, List(), List(), origTweet)
    TweetData.all = tweet :: TweetData.all
    tweet
  }

  def response(data: Any) = Map("code" -> 200, "data" -> data)

  def response(code: Int, error: String) = Map("code" -> code, "error" -> error)

  error {
    case HTTPException(code, msg) => response(code, msg)
    case e: Throwable =>
      response(500, e.getLocalizedMessage)
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

  // get one tweet
  get("/tweets/:id") {
    val me = auth()

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
    val userId = params.get("id")
    if (userId.isDefined) {
      val user = users.find(u => u.id == userId.get.toInt)
      if (user.isDefined) {
        response(user.get)
      } else {
        response(404, "The user with such id is not found")
      }
    } else {
      response(404, "User id is missing")
    }
  }
  // get user tweets
  get("/users/:id/tweets") {
    val me = auth()
    val userId = params.get("id")
    if (userId.isDefined) {
      val user = users.find(u => u.id == userId.get.toInt)
      if (user.isDefined) {
        val userTweets = tweets.filter(t => t.owner.id == userId.get.toInt)
        response(userTweets)
      } else {
        response(404, "The user with such id is not found")
      }
    } else {
      response(404, "User id is missing")
    }
  }

  post("/users/:id/subscribe") {
    val me = auth()

  }

  post("/users/:id/unsubscribe") {
    val me = auth()

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
    val user = users.find(u => u.nickname == nickname && u.password == password).getOrElse(throw HTTPException(400, "User not exist or password incorrect"))

    val (token, salt) = genToken(user.nickname)
    users = user.copy(salt = salt) :: users.filterNot(u => u.nickname == user.nickname)
    response(Map("token" -> token))
  }

  post("/logout") {
    val me = auth()
    users = users.filterNot(u => u.nickname == me.nickname)
    users = me.copy(salt = genSalt()) :: users
    response(Map())
  }
}
