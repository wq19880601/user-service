package com.nm.user.core.finatra

import javax.inject.{Inject, Singleton}

import com.nm.user.common.exceptions.BusinessException
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.exceptions.ExceptionMapper
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.inject.Logging

@Singleton
class BusinessExceptionMapper @Inject()(response: ResponseBuilder)
  extends ExceptionMapper[BusinessException] with Logging {

  override def toResponse(request: Request, exception: BusinessException): Response = {
    error("exception found", exception)
    response.badRequest(exception.getMessage)
  }
}