package com.nm.user.app.v1.userCenter.dto

import java.util.Date

case class UserCenterBaseDto(uid: String, nick: Option[String] = None, iconUrl: Option[String] = None, uName: String,
                             mobile: Option[Long] = None, gender: Option[Int] = None, email: Option[String] = None,
                             birthDay: Option[Date], isFrozen: Int)

