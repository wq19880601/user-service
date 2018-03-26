package com.nm.user.app.v1.deliveryAddress.dao

import javax.inject.{Inject, Singleton}

import com.nm.user.app.v1.deliveryAddress.dto.{DeliveryAddressDto, DeliveryAddressQueryFilter}
import com.nm.user.core.quill.{QuillExtensions, TableSchema}
import com.nm.user.modules.QuillDatabaseModule.QuillDatabaseSource
import com.twitter.inject.Logging
import com.twitter.util.Future


@Singleton
class DeliveryAddressRepository @Inject()(val ctx: QuillDatabaseSource) extends QuillExtensions with TableSchema with Logging {

  import ctx._


  def insert(deliveryAddressDto: DeliveryAddressDto): Future[Int] = {
    implicit val deliverAddressInsertMeta = insertMeta[DeliveryAddress](_.id, _.isDeleted)
    val da = DeliveryAddress(-1, deliveryAddressDto.uid, deliveryAddressDto.province, deliveryAddressDto.city,
      deliveryAddressDto.receiverUser, deliveryAddressDto.receiverMobile, deliveryAddressDto.addrDistinct, deliveryAddressDto.addrDetail,
      deliveryAddressDto.isDefault, deliveryAddressDto.areaCode, -1, getCurrentDate(), getCurrentDate())
    val insertResult = quote {
      deliveryAddressTable.insert(lift(da)).returning(_.id)
    }
    ctx.run(insertResult)
  }

  def selectAll(uid: Int): Future[Seq[DeliveryAddress]] = {
    val queryAll = quote {
      deliveryAddressTable.filter(p => p.uid == lift(uid) && p.isDeleted == 0)
    }
    ctx.run(queryAll)
  }


  def findById(id: Int): Quoted[Query[DeliveryAddress]] = quote {
    deliveryAddressTable.filter(_.id == lift(id))
  }

  def updateByPrimary(deliveryAddressDto: DeliveryAddressDto): Future[Long] = {
    implicit val deliveryUpdateMetaData = insertMeta[DeliveryAddress](_.id)
    val deliverAddress: Future[Option[DeliveryAddress]] = ctx.run {
      findById(deliveryAddressDto.id)
    }.map(_.headOption)

    val result: Future[Long] = deliverAddress.flatMap { deliverAddress =>
      val option: Option[Future[Long]] = for {
        d <- deliverAddress
      } yield {
        var obj = d
        if (deliveryAddressDto.city > 0)
          obj = d.copy(city = deliveryAddressDto.city)
        if (deliveryAddressDto.province > 0)
          obj = obj.copy(province = deliveryAddressDto.province)

        for (ac <- deliveryAddressDto.areaCode)
          obj = obj.copy(areaCode = Some(ac))

        for {
          dc <- Option(deliveryAddressDto.addrDetail)
          if dc.nonEmpty
        } {
          obj = obj.copy(addrDetail = dc)
        }

        if (deliveryAddressDto.receiverMobile > 0)
          obj = obj.copy(receiverMobile = deliveryAddressDto.receiverMobile)

        for (ru <- deliveryAddressDto.receiverUser)
          obj = obj.copy(receiverUser = Some(ru))

        info(s"update by primary, obj=$obj")

        val updateResult = quote {
          deliveryAddressTable.filter(x => x.id == lift(deliveryAddressDto.id) && x.isDeleted == 0).update(lift(obj))
        }
        val result: Future[Long] = ctx.run(updateResult)
        result
      }
      option match {
        case Some(r) => r
        case _ => Future.exception(new RuntimeException(s"not found,id=${deliveryAddressDto.id}"))
      }
    }
    result
  }

  def updateDefault(id: Int, isDefault: Byte): Future[Long] = {
    val updateDefaultR = quote {
      deliveryAddressTable.filter(x => x.id == lift(id) && x.isDeleted == 0).update(_.isDefault -> lift(isDefault), _.modifyTime -> lift(getCurrentDate()))
    }
    ctx.run(updateDefaultR)
  }

  def selectByPrimary(id: Int): Future[Option[DeliveryAddress]] = {
    ctx.run(findById(id)).map(_.headOption)
  }

  def delete(id: Int): Future[Long] = {
    val deleteById = quote {
      deliveryAddressTable.filter(_.id == lift(id)).update(_.isDeleted -> 1)
    }
    ctx.run(deleteById)
  }

  def query(deliveryAddressQueryFilter: DeliveryAddressQueryFilter): Future[Seq[DeliveryAddress]] = {

    val q = deliveryAddressTable
    val cityFilter = deliveryAddressQueryFilter.cityId.map { cityId =>
      quote {
        q.filter(_.city == lift(cityId))
      }
    }.getOrElse(q)

    val provinceFiliter = deliveryAddressQueryFilter.province.map { province =>
      quote {
        cityFilter.filter(_.province == lift(province))
      }
    }.getOrElse(cityFilter)

    val usreFilter = deliveryAddressQueryFilter.receiveUser.map { user =>
      quote {
        provinceFiliter.filter(_.receiverUser.exists(_ like lift(user)))
      }
    }.getOrElse(provinceFiliter)


    val result: ctx.Quoted[ctx.Query[DeliveryAddress]] =
      quote {
        usreFilter.sortBy(_.createTime)(Ord.desc).drop(lift(deliveryAddressQueryFilter.pageRequest.getOffset())).take(lift(deliveryAddressQueryFilter.pageRequest.pageSize))
      }

    ctx.run(result)
  }

}
