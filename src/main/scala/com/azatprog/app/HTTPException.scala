package com.azatprog.app

case class HTTPException(code: Int, error: String) extends Throwable
