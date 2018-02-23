package com.azatprog.app.models

case class User(
                 id: Int = java.util.UUID.randomUUID.hashCode(),
                 email: String,
                 nickname: String,
                 password: String,
                 salt: String = java.util.UUID.randomUUID.toString,
                 subscriptions: List[User] = List()
               )

object User {
  def map(u: User) = Map(
    "id" -> u.id,
    "email" -> u.email,
    "nickname" -> u.nickname,
    "subscriptions" -> u.subscriptions.map(User.shortMap)
  )

  def shortMap(u: User) = Map(
    "id" -> u.id,
    "nickname" -> u.nickname
  )
}

object UserData {

  var all = List(
    User(email = "dilyis@email.com", nickname = "dilyis", password = "xxx"),
    User(email = "mitya@email.com", nickname = "mitya", password = "xxx"),
    User(email = "azatprog@email.com", nickname = "azatprog", password = "xxx"),
  )

  def getAllUsers: List[User] = all

  def getUserById(id: Int): Option[User] = all.find(_.id == id)

  def getUserByLogin(nickname: String): Option[User] = all.find(_.nickname == nickname)

}