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
    "date" -> t.date,
    "text" -> t.text,
    "mentioned" -> t.mentioned.map(User.shortMap),
    "likes" -> t.likes.map(User.shortMap),
    "dislikes" -> t.dislikes.map(User.shortMap)
  )
}

object TweetData {

  var all = List(
    Tweet(owner = UserData.getAllUsers.head, text = "Hey"),
    Tweet(owner = UserData.getAllUsers.head, text = "Ho"),
    Tweet(owner = UserData.getAllUsers.head, text = "Let's go!")
  )

  def getAllTweets: List[Tweet] = all

  def getTweetById(id: Int): Option[Tweet] = all.find(_.id == id)

  def getTweetsByOwner(owner: User): List[Tweet] = all.filter(_.owner == owner)

}