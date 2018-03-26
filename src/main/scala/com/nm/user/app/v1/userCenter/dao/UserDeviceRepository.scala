package com.nm.user.app.v1.userCenter.dao

import javax.inject.{Inject, Singleton}

import com.nm.user.app.v1.userCenter.dto.UserDeviceInfoDto
import com.nm.user.core.quill.{QuillExtensions, TableSchema}
import com.nm.user.modules.QuillDatabaseModule.QuillDatabaseSource
import com.twitter.inject.Logging
import com.twitter.util.Future

@Singleton
class UserDeviceRepository @Inject()(val ctx: QuillDatabaseSource) extends QuillExtensions with TableSchema with Logging {

  import ctx._

  def saveDeviceInfo(userDeviceInfoDto: UserDeviceInfoDto): Future[Long] = {
    val currentDate = getCurrentDate()
    val userDeviceInfo = UserDeviceInfo(id = -1, appVersion = userDeviceInfoDto.appVersion, deviceId = userDeviceInfoDto.deviceId, deviceName = userDeviceInfoDto.deviceName,
      imie = userDeviceInfoDto.imie, uid = userDeviceInfoDto.uid, idfa = userDeviceInfoDto.idfa, systemVersion = userDeviceInfoDto.systemVersion,
      macAddr = userDeviceInfoDto.macAddr, deviceToken = userDeviceInfoDto.deviceToken, uuid = userDeviceInfoDto.uuid, ipAddr = userDeviceInfoDto.ipAddr,
      systemInfo = userDeviceInfoDto.systemInfo, createTime = currentDate, modifyTime = currentDate)
    ctx.run {
      quote {
        userDeviceInfoTable.insert(userDeviceInfo)
      }
    }
  }
}
