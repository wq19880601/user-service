package com.nm.user.app.v1.userCenter.dto

import java.util.Date

case class UserTpSourceDto(id: Int, uid: String, account: String, sourceType: Int, nick: String, tpUid: String, openId: String,
                           iconUrl: Option[String] = None, gender: Option[String] = None, accessToken: Option[String] = None, refreshToken: Option[String] = None,
                           expiration: Option[String] = None, createTime: Option[Date] = None, modifyTime: Date)
