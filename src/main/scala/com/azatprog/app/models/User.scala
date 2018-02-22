package com.azatprog.app.models

case class User(
                 id: Int = java.util.UUID.randomUUID.hashCode(),
                 email: String,
                 nickname: String,
                 password: String,
                 salt: String,
                 subscriptions: List[User]
               )