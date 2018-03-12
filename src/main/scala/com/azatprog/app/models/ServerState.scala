package com.azatprog.app.models

trait ServerState {
  protected var users: List[User] = List()
  protected var tweets: List[Tweet] = List()
}
