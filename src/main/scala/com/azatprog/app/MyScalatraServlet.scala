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

  private def isValidToken(jwt: String, salt: String): Boolean = {
    JsonWebToken.validate(jwt, salt)
  }

  private def auth(token: String) {
    val nickname = parseTokenAndGetValue(token, "nickname")
    if (!isValidToken(token, "asd")) {
      halt(401)
    }
    nickname
  }

  private def parseTokenAndGetValue(jwt: String, key: String): String = {
    val claims: Option[Map[String, String]] = jwt match {
      case JsonWebToken(header, claimsSet, signature) =>
        claimsSet.asSimpleMap.toOption
      case x =>
        None
    }
    val parsedToken: Map[String, String] = claims match {
      case Some(value) => value
      case _ => Map.empty
    }
    if (parsedToken.isEmpty) {
      ""
    } else {
      parsedToken(key)
    }
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

  // get one tweet
  get("/tweets/:id") {
    //    params.getOrElse("id", halt(400)).toInt

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

  }
  // get user tweets
  get("/users/:id/tweets") {

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
    } else {
      val (token, salt) = genToken(nickname.get)
      val newUser = User(email = email.get, nickname = nickname.get, password = password.get, salt = salt, subscriptions = List())
      users = newUser :: users
      response(Map("token"-> token))
    }
  }

  post("/login") {

  }

  post("/logout") {

  }
}
