package com.nm.user.app.v1.userCenter.dto

import com.twitter.finatra.validation.NotEmpty

case class UserMobileLoginDto(mobile:Long, @NotEmpty passwd:String)
