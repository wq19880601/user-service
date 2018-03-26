package com.nm.user.app.v1.deliveryAddress.service

import javax.inject.{Inject, Singleton}

import com.nm.user.app.v1.deliveryAddress.dao.{DeliveryAddress, DeliveryAddressRepository}
import com.nm.user.app.v1.deliveryAddress.dto.{DeliveryAddressDto, DeliveryAddressQueryFilter}
import com.nm.user.common.enums.DefaultType
import com.twitter.util.Future


@Singleton
class DeliverAddressService @Inject()(val deliveryAddressRepository: DeliveryAddressRepository) {

  def save(deliveryAddressDto: DeliveryAddressDto): Future[Int] = {
    deliveryAddressRepository.insert(deliveryAddressDto)
  }

  def updateDefault(id: Int, defaultType: DefaultType): Future[Long] = {
    deliveryAddressRepository.updateDefault(id, defaultType.getCode().toByte)
  }

  def selectByPrimaryKey(id: Int): Future[Option[DeliveryAddress]] = {
    deliveryAddressRepository.selectByPrimary(id)
  }

  def delete(id: Int): Future[Long] = {
    deliveryAddressRepository.delete(id)
  }

  def updateByPrimaryKey(deliveryAddressDto: DeliveryAddressDto): Future[Long] = {
    deliveryAddressRepository.updateByPrimary(deliveryAddressDto)
  }

  def selectAll(uid: Int): Future[Seq[DeliveryAddressDto]] = {
    val deliverAddressF: Future[Seq[DeliveryAddress]] = deliveryAddressRepository.selectAll(uid)
    convert(deliverAddressF)
  }

  private def convert(deliverAddressF:Future[Seq[DeliveryAddress]]):Future[Seq[DeliveryAddressDto]] = {
    val value: Future[Seq[DeliveryAddressDto]] = for {
      deliverAddress <- deliverAddressF
    } yield {
      deliverAddress.map(x => DeliveryAddressDto(x.id, x.uid, x.province, x.city, x.addrDistinct, x.addrDetail,
        x.areaCode, x.receiverUser, x.receiverMobile, x.isDefault)
      )
    }
    value
  }

  def query(queryFilter: DeliveryAddressQueryFilter):Future[Seq[DeliveryAddressDto]] = {
    val deliverAddressF: Future[Seq[DeliveryAddress]] = deliveryAddressRepository.query(queryFilter)
    convert(deliverAddressF)
  }

}
