package com.nm.user.app.v1.userCenter.dto

import java.util.Date

case class UserInfoDto(id: Int, uid: String,
                    nick: Option[String] = None, iconUrl: Option[String] = None, uName: String, uPass: Option[String] =None,
                    mobile: Option[Long] = None, gender: Option[Int] = None, email: Option[String]=None,lastLoginTime:Option[Date] = None,
                    birthDay: Option[Date] = None, appVersion: Option[String] = None, isFrozen: Int = 0);
