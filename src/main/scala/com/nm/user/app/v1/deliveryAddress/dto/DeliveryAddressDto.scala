package com.nm.user.app.v1.deliveryAddress.dto

case class DeliveryAddressDto(id:Int, uid: Int, province: Int, city: Int, addrDistinct: String, addrDetail: String,
                              areaCode: Option[Int] = None, receiverUser: Option[String], receiverMobile: Int, isDefault:Byte)



