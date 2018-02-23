package com.azatprog.app.models

case class Tweet(
                  id: Int = java.util.UUID.randomUUID.hashCode(),
                  owner: User,
                  date: Long = System.currentTimeMillis() / 1000,
                  text: String,
                  mentioned: List[User] = List(),
                  likes: List[User] = List(),
                  dislikes: List[User] = List(),
                  origTweet: Option[Tweet] = None
                )

object Tweet {
  def map(t: Tweet) = Map(
    "id" -> t.id,
    "owner" -> User.shortMap(t.owner),
    "date" -> t.date,
    "text" -> t.text,
    "mentioned" -> t.mentioned.map(User.shortMap),
    "likes" -> t.likes.map(User.shortMap),
    "dislikes" -> t.dislikes.map(User.shortMap),
    "origTweet" -> (if (t.origTweet.isDefined) Tweet.shortMap(t.origTweet.get) else Map())
  )

  def shortMap(t: Tweet) = Map(
    "id" -> t.id,
    "owner" -> User.shortMap(t.owner),
    "date" -> t.date,
    "text" -> t.text,
    "likesCount" -> t.likes.length,
    "dislikesCount" -> t.dislikes.length,
  )
}