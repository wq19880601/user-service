package com.nm.user.app.v1.securityVerify.dto

case class SmsCodeGetDto(smsVerifySign:Option[String] = None, errorMsg:Option[String] = None)
