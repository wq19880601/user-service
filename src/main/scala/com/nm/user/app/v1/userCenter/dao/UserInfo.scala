package com.nm.user.app.v1.userCenter.dao

import java.util.Date

case class UserInfo(id: Int, uid: String,
                    nick: Option[String] = None, iconUrl: Option[String], uName: String, uPass: Option[String] = None,
                    mobile: Option[Long] = None, gender: Option[Int] = None, email: Option[String] = None,
                    birthDay: Option[Date] = None, appVersion: Option[String] = None, isFrozen: Byte = 0, lastLoginTime:Option[Date] = None, createTime: Option[Date] = None, modifyTime: Date)
