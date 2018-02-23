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

  // Находясь в здравом уме и трезвой памяти,
  // осознаём какая дичь тут написана, но лучше решение не гуглиться
  // сорян за боль и слёзы
  var currentUser: User = _

  before("""^\/(?!(?:register|login)).*""".r) {
    contentType = formats("json")
    val token = params.getOrElse("token", throw HTTPException(401, "Unauthorized, token parameter is missing"))
    val jwt = JsonWebToken.unapply(token).getOrElse(throw HTTPException(400, "Invalid token parameter"))
    val nickname = jwt._2.asSimpleMap.toOption
      .getOrElse(throw HTTPException(400, "Invalid token parameter"))
      .getOrElse("nickname", throw HTTPException(400, "Invalid token parameter"))
    val user = users.find(u => u.nickname == nickname).getOrElse(throw HTTPException(400, "Invalid token parameter"))
    if (!JsonWebToken.validate(token, user.salt))
      throw HTTPException(400, "Invalid token parameter")
    currentUser = user
  }
  after() {
    currentUser = _
  }

  // get one tweet
  get("/tweets/:id") {

  }

  // edit tweet
  put("/tweets/:id") {

  }

  // like tweet, dont foget to remove dislike
  put("/tweets/:id/like") {

  }

  // remove like from tweet
  delete("/tweets/:id/like") {

  }

  // dislike tweet, dont foget to remove like
  put("/tweets/:id/dislike") {

  }

  // remove dislike from tweet
  delete("/tweets/:id/dislike") {

  }

  // delete, dont forget to delete retweets
  delete("/tweets/:id") {

  }

  //retweet, auto mention of owner
  post("/tweets/:id/retweet") {

  }

  //
  // FEED
  //
  get("/feed") {

  }

  //
  // USERS
  //
  get("/users/:id") {
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

  }

  post("/users/:id/unsubscribe") {

  }

  //
  // AUTH
  //
  post("/register") {
    val nickname = params.get("nickname")
    val email = params.get("email")
    val password = params.get("password")
    if (nickname.isEmpty || email.isEmpty || password.isEmpty ) {
      response(404, "One of the parameters is missing: nickname, email, password")
    } else if (users.exists( u => u.email == email.get || u.nickname == nickname.get)) {
        response(400, "The user is already exists")
      }else{
        val (token, salt) = genToken(nickname.get)
        val newUser = User(email = email.get, nickname = nickname.get, password = password.get, salt = salt, subscriptions = List())
        users = newUser :: users
        response(Map("token"-> token))
    }
  }

  post("/login") {

  }

  post("/logout") {
    val token = params.get("token").get
    val nickname = parseTokenAndGetValue(token, "nickname")
    val user = users.find(u => u.nickname == nickname).get.copy(salt = genSalt())
    users = users.filterNot(u => u.nickname == nickname)
    users = user :: users
    response(Map())
  }
}
