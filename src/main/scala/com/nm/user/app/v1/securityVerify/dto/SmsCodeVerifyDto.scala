package com.nm.user.app.v1.securityVerify.dto

case class SmsCodeVerifyDto(status:Int, content:Option[String] = None)
