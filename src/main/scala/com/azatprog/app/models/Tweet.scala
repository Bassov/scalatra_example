package com.azatprog.app.models

import java.util.Date

case class Tweet(
                  id: Int = java.util.UUID.randomUUID.hashCode(),
                  owner: User,
                  date: java.util.Date,
                  text: String,
                  mentioned: List[User],
                  likes: List[User],
                  dislikes: List[User],
                  origTweet: Option[Tweet]
                )

case class TweetForm(
                      owner: Int,
                      text: String,
                      origTweet: Option[Int]
                    )

object TweetData {

  var all = List(
    Tweet(1, UserData.getUserById(1).get, new Date(), "Hi",
      List(), List(), List(), None)
  )

}