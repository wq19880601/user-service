package com.nm.user.core.quill

import java.util.Date

import com.nm.user.app.v1.deliveryAddress.dao.DeliveryAddress
import com.nm.user.app.v1.userCenter.dao.{UserDeviceInfo, UserInfo, UserToken, UserTpSource}
import com.nm.user.modules.QuillDatabaseModule.QuillDatabaseSource


trait TableSchema {

  val ctx: QuillDatabaseSource

  import ctx._

  val deliveryAddressTable: ctx.Quoted[ctx.EntityQuery[DeliveryAddress]] = quote {
    querySchema[DeliveryAddress]("delivery_address",
      _.id -> "id", _.isDefault -> "is_default", _.receiverMobile -> "receiver_mobile",
      _.areaCode -> "area_code", _.addrDetail -> "addr_detail", _.receiverUser -> "receiver_user",
      _.addrDistinct -> "addr_distinct", _.province -> "province", _.city -> "city", _.isDeleted -> "is_deleted",
      _.createTime -> "create_time", _.modifyTime -> "modify_time")
  }


  val userInfoTable: ctx.Quoted[ctx.EntityQuery[UserInfo]] = quote {
    querySchema[UserInfo]("user_info",
      _.id -> "id", _.appVersion -> "app_version", _.birthDay -> "birth_day",
      _.email -> "email", _.gender -> "gender", _.iconUrl -> "icon_url",
      _.isFrozen -> "is_frozen", _.nick -> "nick", _.uid -> "uid", _.uName -> "u_name", _.uPass -> "u_pass",
      _.createTime -> "create_time", _.modifyTime -> "modify_time",_.lastLoginTime -> "last_login_time")
  }

  val userTokenTable: ctx.Quoted[ctx.EntityQuery[UserToken]] = quote {
    querySchema[UserToken]("user_token",
      _.id -> "id", _.uid -> "uid", _.deviceId -> "device_id",
      _.expiration -> "expiration", _.token -> "token", _.refreshToken -> "refresh_token",
      _.createTime -> "create_time", _.modifyTime -> "modify_time")
  }


  val userTpSourceTable: ctx.Quoted[ctx.EntityQuery[UserTpSource]] = quote {
    querySchema[UserTpSource]("user_tp_source",
      _.id -> "id", _.uid -> "uid", _.account -> "account", _.sourceType -> "source_type", _.nick -> "nick",
      _.tpUid -> "tp_Uid", _.openId -> "open_id", _.iconUrl -> "icon_url", _.gender -> "gender", _.accessToken -> "access_token",
      _.refreshToken -> "refresh_token", _.expiration -> "expiration", _.createTime -> "create_time", _.modifyTime -> "modify_time",_.isDeleted -> "")
  }


  val userDeviceInfoTable: ctx.Quoted[ctx.EntityQuery[UserDeviceInfo]] = quote {
    querySchema[UserDeviceInfo]("user_device_info",
      _.id -> "id", _.uid -> "uid", _.appVersion -> "app_version", _.deviceId -> "device_id", _.deviceName -> "device_name",
      _.imie -> "imie", _.idfa -> "idfa", _.systemInfo -> "system_info", _.systemVersion -> "system_version", _.macAddr -> "mac_addr",
      _.uuid -> "uuid", _.createTime -> "create_time", _.modifyTime -> "modify_time",_.isDeleted -> "is_deleted")
  }


  def getCurrentDate(): Date = new Date()

}
