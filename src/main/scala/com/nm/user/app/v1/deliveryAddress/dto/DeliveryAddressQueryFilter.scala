package com.nm.user.app.v1.deliveryAddress.dto

import com.nm.user.common.domain.PageRequest

case class DeliveryAddressQueryFilter(cityId:Option[Int] = None, province:Option[Int] = None, receiveUser:Option[String] = None, pageRequest: PageRequest = PageRequest())


