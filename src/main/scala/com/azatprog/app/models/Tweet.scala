package com.azatprog.app.models

case class Tweet(
                  id: Int = java.util.UUID.randomUUID.hashCode(),
                  owner: User,
                  date: Int,
                  text: String,
                  mentioned: List[User],
                  likes: List[User],
                  dislikes: List[User],
                  origTweet: Option[Tweet]
                )
