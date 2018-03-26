package com.nm.user.app.v1.userCenter.dao

import java.util.Date

case class UserDeviceInfo(id: Int, uid: Int, appVersion: String, deviceId: Option[String], deviceName: Option[String],
                          imie: Option[String], idfa: Option[String], systemVersion: Option[String], macAddr: Option[String], deviceToken: Option[String],
                          uuid: Option[String], ipAddr: Option[String], systemInfo: Option[String], createTime: Date, modifyTime: Date, isDeleted: Byte = 0)

  extends BaseInfo(createTime, modifyTime, isDeleted)
