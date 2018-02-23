package com.azatprog.app.models

case class User(
                 id: Int = java.util.UUID.randomUUID.hashCode(),
                 email: String,
                 nickname: String,
                 password: String,
                 salt: String,
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
    User(1, "email", "dilyis", "xxx", "123", List())
  )

  def getAllUsers: List[User] = all

  def getUserById(id: Int): Option[User] = all.find(_.id == id)

  def getUserByLogin(nickname: String): Option[User] = all.find(_.nickname == nickname)

}