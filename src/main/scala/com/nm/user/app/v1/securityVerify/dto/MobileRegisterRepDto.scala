package com.nm.user.app.v1.securityVerify.dto

import com.nm.user.common.enums.MobileCheckType
import com.twitter.finatra.validation.{MethodValidation, NotEmpty, ValidationResult}

case class MobileRegisterReqDto(mobile: Long, @NotEmpty password: String, @NotEmpty smsCode: String, @NotEmpty verifySign: String, @NotEmpty mobileCheckType: String) {

  @MethodValidation
  def validateMobileCheckType = {
    val mobileCheckTypeOpt: Option[MobileCheckType] = MobileCheckType.values().find(x => x.getType == mobileCheckType)
    ValidationResult.validate(
      mobileCheckTypeOpt.nonEmpty,
      "invalid type")
  }
}
