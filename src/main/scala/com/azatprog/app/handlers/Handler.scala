package com.azatprog.app.handlers

import com.azatprog.app.models.ServerState
import org.json4s.{DefaultFormats, Formats}
import org.scalatra.ScalatraServlet
import org.scalatra.json.JacksonJsonSupport

trait Servlet extends ScalatraServlet with JacksonJsonSupport with ServerState {
  before() {
    contentType = formats("json")
  }

  protected implicit lazy val jsonFormats: Formats = DefaultFormats
}

trait Handler extends Servlet {
  protected sealed trait ServerException
  protected case class GenericException(code: Int, text: String) extends ServerException
  protected case class ParamError(param: String) extends ServerException

  protected type Action[A] = Either[ServerException, A]

  protected def response(code: Int, error: String) = Map("code" -> code, "error" -> error)

  protected def makeHandler[A](handler: Action[A]): Any = handler match {
    case Right(x) => x
    case Left(GenericException(code, msg)) => response(code, msg)
    case Left(ParamError(param)) => response(400, "Missing parameter " ++ param)
  }

  protected def getParam(str: String): Action[String] = {
    Right(params.getOrElse(str, return Left(ParamError(str))))
  }

  protected def getParams = params

  protected def validate(cond: Boolean)(err: ServerException) = if (cond) Left(err) else Right(())

  protected def pure[A](el: A): Action[A] = Right(el)
}
