package com.nm.user.app.v1.userCenter.dto

case class UserDeviceInfoDto(id: Int, uid: Int, appVersion: String, deviceId: Option[String], deviceName: Option[String],
                             imie: Option[String], idfa: Option[String], systemVersion: Option[String], macAddr: Option[String], deviceToken: Option[String],
                             uuid: Option[String], ipAddr: Option[String], systemInfo: Option[String])
