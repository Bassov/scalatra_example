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