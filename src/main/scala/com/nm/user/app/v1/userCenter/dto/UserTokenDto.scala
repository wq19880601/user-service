package com.nm.user.app.v1.userCenter.dto

import java.util.Date

case class UserTokenDto(id: Int, uid: String, deviceId: Option[String] = None, token: String,
                        refreshToken: Option[String] =None, expiration: Option[Date] = None,
                        createTime: Date, modifyTime: Date)
