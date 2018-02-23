package com.azatprog.app

import models._
import org.scalatra._
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json._

import authentikat.jwt.JwtHeader
import authentikat.jwt.JwtClaimsSet
import authentikat.jwt.JsonWebToken

class MyScalatraServlet extends ScalatraServlet with JacksonJsonSupport {

  val DEBUG = true

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

  private var users: List[User] = List()
  private var tweets: List[Tweet] = List()

  if (DEBUG) (() => {
    val azatprog = User(id = 0, email = "azatprog@email.com", nickname = "azatprog", password = "xxx", salt = genSalt())
    val dilyis = User(id = 1, email = "dilyis@email.com", nickname = "dilyis", password = "xxx", salt = genSalt(), subscriptions = List(azatprog))
    val mitya = User(id = 2, email = "mitya@email.com", nickname = "mitya", password = "xxx", salt = genSalt(), subscriptions = List(azatprog, dilyis))
    users = List(azatprog, dilyis, mitya)
    tweets = List(
      Tweet(id = 0, owner = azatprog, text = "Hey"),
      Tweet(id = 1, owner = dilyis, text = "Ho"),
      Tweet(id = 2, owner = mitya, text = "Let's go!")
    )
  }) ()

  private def genSalt() = if (DEBUG) "it's only for create postman autotests!" else java.util.UUID.randomUUID.toString

  private def genToken(nickname: String): (String, String) = {
    val header = JwtHeader("HS256")
    val claimsSet = JwtClaimsSet(Map(
      "nickname" -> nickname,
      "createTime" -> (if (DEBUG) 0 else System.currentTimeMillis() / 1000)
    ))
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

  def parse(text: String): List[User] = {
    var mentioned = List[User]()
    val words = text.split(" ")
    for (w <- words) {
      if (w.startsWith("@")) {
        val user = users
          .find(u => u.nickname == w.substring(1))
        if (user.isDefined) {
          mentioned = user.get :: mentioned
        }
      }
    }
    mentioned
  }

  //
  // Tweets
  //
  // tweet the tweet
  post("/tweets") {
    val me = auth()
    val text = params.getOrElse("text", throw HTTPException(400, "Missing parameter text"))
    val mentioned = parse(text)
    val tweet = new Tweet(owner = me, text = text, mentioned = mentioned)
    tweets = tweet :: tweets
    response(Tweet.map(tweet))
  }

  // get one tweet
  get("/tweets/:id") {
    val me = auth()
    try {
      val tweetId = params("id").toInt
      val tweet = tweets.find(_.id == tweetId).getOrElse(throw HTTPException(400, "Wrong format id"))
      response(Tweet.map(tweet))
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrong id format")
    }
  }

  // edit tweet
  put("/tweets/:id") {
    val me = auth()
    val text = params.getOrElse("text", throw HTTPException(400, "Missing parameter text"))
    val mentioned = parse(text)
    try {
      val tweetId = params("id").toInt
      val tweet = tweets
        .find(t => t.id == tweetId && t.owner.id == me.id)
        .getOrElse(throw HTTPException(400, "You are not the owner of the tweet"))
      tweets = tweet.copy(text = text, mentioned = mentioned) :: tweets.filterNot(_ == tweet)
      response(Tweet.map(tweets.find(_.id == tweetId).get))
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrong id format")
    }
  }

  // like tweet, dont foget to remove dislike
  put("/tweets/:id/like") {
    val me = auth()
    try {
      val tweetId = params("id").toInt
      val tweet = tweets.find(_.id == tweetId).getOrElse(throw HTTPException(404, "Tweet not found"))
      tweets = tweet
        .copy(likes = me :: tweet.likes.filterNot(_ == me), dislikes = tweet.dislikes.filterNot(_ == me)) ::
        tweets.filterNot(_ == tweet)
      response(Tweet.map(tweets.find(_.id == tweetId).get))
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrong id format")
    }
  }

  // remove like from tweet
  delete("/tweets/:id/like") {
    val me = auth()
    try {
      val tweetId = params("id").toInt
      val tweet = tweets.find(_.id == tweetId).getOrElse(throw HTTPException(404, "Tweet not found"))
      tweets = tweet
        .copy(likes = tweet.likes.filterNot(_ == me), dislikes = tweet.dislikes.filterNot(_ == me)) ::
        tweets.filterNot(_ == tweet)
      response(Tweet.map(tweets.find(_.id == tweetId).get))
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrong id format")
    }
  }

  // dislike tweet, dont foget to remove like
  put("/tweets/:id/dislike") {
    val me = auth()
    try {
      val tweetId = params("id").toInt
      val tweet = tweets.find(_.id == tweetId).getOrElse(throw HTTPException(404, "Tweet not found"))
      tweets = tweet
        .copy(likes = tweet.likes.filterNot(_ == me), dislikes = me :: tweet.dislikes.filterNot(_ == me)) ::
        tweets.filterNot(_ == tweet)
      response(Tweet.map(tweets.find(_.id == tweetId).get))
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrong id format")
    }
  }

  // remove dislike from tweet
  delete("/tweets/:id/dislike") {
    val me = auth()
    try {
      val tweetId = params("id").toInt
      val tweet = tweets.find(_.id == tweetId).getOrElse(throw HTTPException(404, "Tweet not found"))
      tweets = tweet
        .copy(likes = tweet.likes.filterNot(_ == me), dislikes = tweet.dislikes.filterNot(_ == me)) ::
        tweets.filterNot(_ == tweet)
      response(Tweet.map(tweets.find(_.id == tweetId).get))
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrong id format")
    }
  }

  // delete, dont forget to delete retweets
  delete("/tweets/:id") {
    val me = auth()
    try {
      val tweetId = params("id").toInt
      val tweet = tweets
        .find(t => t.id == tweetId && t.owner.id == me.id)
        .getOrElse(throw HTTPException(400, "You are not the owner of the tweet"))
      tweets = tweets.filterNot(t => t == tweet || t.origTweet.getOrElse(None) == tweet)
      response()
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrong id format")
    }
  }

  //retweet, auto mention of owner
  post("/tweets/:id/retweet") {
    val me = auth()
    val text = params.getOrElse("text", throw HTTPException(400, "Missing parameter text"))
    val mentioned = parse(text)
    try {
      val tweetId = params("id").toInt
      val origTweet = tweets
        .find(_.id == tweetId)
        .getOrElse(throw HTTPException(400, "Wrong format id"))
      val tweet = new Tweet(
        owner = me, text = text,
        origTweet = Option(origTweet),
        mentioned = origTweet.owner :: mentioned
      )
      tweets = tweet :: tweets
      response(Tweet.map(tweet))
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrong id format")
    }
  }

  //
  // FEED
  //
  get("/feed") {
    val me = auth()
    val feed: List[Tweet] = (me.subscriptions
      .flatMap(sub => tweets.filter(t => t.owner.id == sub.id)) :::
      tweets.filter(_.mentioned.contains(me))
      ).sortBy(_.date)

    response(Map(
      "feed" -> feed.map(Tweet.map)
    ))
  }

  //
  // USERS
  //
  get("/users") {
    val me = auth()
    response(users.map(User.shortMap))
  }

  get("/users/:id") {
    val me = auth()
    try {
      val userId = params("id").toInt
      val user = users.find(_.id == userId).getOrElse(throw HTTPException(400, "Wrong format id"))
      response(User.map(user))
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrong id format")
    }
  }

  // get user tweets
  get("/users/:id/tweets") {
    val me = auth()
    try {
      val userId = params("id").toInt
      val user = users
        .find(_.id == userId)
        .getOrElse(throw HTTPException(400, "The user with such id is not found"))
      val userTweets = tweets.filter(t => t.owner.id == userId.toInt)
      response(userTweets.map(Tweet.map))
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrong id format")
    }
  }

  post("/users/:id/subscribe") {
    val me = auth()
    try {
      val userId = params("id").toInt
      val user = users
        .find(_.id == userId.toInt)
        .getOrElse(throw HTTPException(400, "The user with such id is not found"))
      val subs = user :: me.subscriptions.filterNot(_ == user)
      users = me.copy(subscriptions = subs) :: users.filterNot(_ == me)
      response()
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrong id format")
    }
  }

  post("/users/:id/unsubscribe") {
    val me = auth()
    try {
      val userId = params("id").toInt
      val user = users
        .find(_.id == userId.toInt)
        .getOrElse(throw HTTPException(400, "The user with such id is not found"))
      val subs = me.subscriptions.filterNot(_ == user)
      users = me.copy(subscriptions = subs) :: users.filterNot(_ == me)
      response()
    } catch {
      case _: NumberFormatException => throw HTTPException(400, "Wrong id format")
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
      throw HTTPException(400, "User is already exists")
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
