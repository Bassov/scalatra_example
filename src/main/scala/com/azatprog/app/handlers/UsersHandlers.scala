package com.azatprog.app.handlers

import com.azatprog.app.models.User

trait UsersHandlers extends Handler with Authentication {
  def usersPost: Action[Map[String,String]] = for {
    nickname <- getParam("nickname")
    email <- getParam("email")
    password <- getParam("password")

    _ <- validate(users.exists(u => u.email == email || u.nickname == nickname))(GenericException(400, "User is already exists"))

    tokenSalt <- pure(genToken(nickname))
    newUser <- pure(User(email = email, nickname = nickname, password = password, salt = tokenSalt._2))
    _ <- pure(users = newUser :: users)
  } yield Map("token" -> tokenSalt._1)

  def usersGet = for {
    _ <- auth()
  } yield users.map(User.shortMap)
}
