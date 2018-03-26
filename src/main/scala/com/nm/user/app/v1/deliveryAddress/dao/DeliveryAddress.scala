package com.nm.user.app.v1.deliveryAddress.dao

import java.util.Date

import com.nm.user.app.v1.userCenter.dao.BaseInfo

case class DeliveryAddress(id: Int, uid: Int, province: Int,
                      city: Int, receiverUser: Option[String] = None,
                      receiverMobile: Int, addrDistinct: String, addrDetail: String,
                      isDefault: Byte = 0, areaCode: Option[Int] = None, isDeleted:Byte,
                      createTime: Date, modifyTime: Date)
  extends BaseInfo(createTime, modifyTime, isDeleted)


