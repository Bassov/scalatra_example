package com.azatprog.app.old_server

case class HTTPException(code: Int, error: String) extends Throwable
