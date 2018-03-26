package com.nm.user.app.v1.response

// status 0 fail, 1 success
case class ResponseDto[T](status: Int = 1, body: Option[T] = None, msg: Option[String] = None)

object ResponseDto {

  def success[T](body: T, msg: Option[String] = None): ResponseDto[T] = {
    ResponseDto(status = 1, body = Some(body), msg = msg)
  }

  def fail[T](msg: Option[String]): ResponseDto[T] = {
    ResponseDto(status = 0, msg = msg)
  }
}
