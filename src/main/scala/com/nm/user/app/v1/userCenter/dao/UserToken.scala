package com.nm.user.app.v1.userCenter.dao

import java.util.Date

case class UserToken(id: Int, uid: String, deviceId: Option[String] = None, token: String,
                     refreshToken: Option[String] =None, expiration: Option[Date] = None,
                     createTime: Date, modifyTime: Date,isDeleted:Byte = 0)
  extends BaseInfo(createTime, modifyTime, isDeleted)
