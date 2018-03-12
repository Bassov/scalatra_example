package com.azatprog.app.handlers

import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}
import com.azatprog.app.models.User

trait Authentication extends Handler {
  val salt = "test-salt"

  def genToken(nickname: String): (String, String) = {
    val header = JwtHeader("HS256")
    val claimsSet = JwtClaimsSet(Map(
      "nickname" -> nickname,
      "createTime" -> 0
    ))
    val jwt: String = JsonWebToken(header, claimsSet, salt)
    (jwt, salt)
  }

  def auth(): Action[User] = {
    val token = params
      .getOrElse("token", return Left(GenericException(401, "Unauthorized, token parameter is missing")))

    val jwt = JsonWebToken.unapply(token)
      .getOrElse(return Left(GenericException(400, "Invalid token parameter")))

    val nickname = jwt._2.asSimpleMap.toOption
      .getOrElse(return Left(GenericException(400, "Invalid token parameter")))
      .getOrElse("nickname", return Left(GenericException(400, "Invalid token parameter")))

    val user = users.find(u => u.nickname == nickname)
      .getOrElse(return Left(GenericException(400, "Invalid token parameter")))

    if (!JsonWebToken.validate(token, user.salt))
      return Left(GenericException(400, "Invalid token parameter"))

    Right(user)
  }
}
