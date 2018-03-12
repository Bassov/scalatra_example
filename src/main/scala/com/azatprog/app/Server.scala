package com.azatprog.app

import com.azatprog.app.handlers.UsersHandlers

object Server extends UsersHandlers {
  post("/users") { makeHandler(usersPost) }
  get("/users")  { makeHandler(usersGet) }
}
