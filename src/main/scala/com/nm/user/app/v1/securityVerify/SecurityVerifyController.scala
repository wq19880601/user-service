package com.nm.user.app.v1.securityVerify

import javax.inject.{Inject, Singleton}

import com.nm.user.app.v1.securityVerify.dto.{ImageCodeDto, InspectSmsCodeReqDto, MobileCheckReqDto}
import com.nm.user.app.v1.securityVerify.service.SecurityVerifyService
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

@Singleton
class SecurityVerifyController @Inject()(val securityVerifyService: SecurityVerifyService) extends Controller {

  prefix("/account") {

    post("/getYzmCode") { request: Request =>
      val cs = securityVerifyService.getImageCode()
      cs
    }

    post("/inspectYzmCode") { request: ImageCodeDto =>
      val result = securityVerifyService.verifyImageCode(request.s, request.code)
      result
    }

    post("/doCheck") { request: MobileCheckReqDto =>
      val result = securityVerifyService.doCheck(request.mobile)
      result
    }

    post("/inspectSmsCode") { request: InspectSmsCodeReqDto =>
      val result = securityVerifyService.verifySmsCode(request.mobile, request.smsCode, request.verifySign)
      result
    }
  }

}
