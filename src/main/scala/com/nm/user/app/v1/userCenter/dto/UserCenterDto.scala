package com.nm.user.app.v1.userCenter.dto

case class UserCenterDto(userInfoDto: Option[UserInfoDto], userTokenDto: Option[UserTokenDto], userTpSourceDto: Seq[UserTpSourceDto])
