package com.nm.user.app.v1.deliveryAddress

import javax.inject.{Inject, Singleton}

import com.nm.user.app.v1.deliveryAddress.dao.DeliveryAddress
import com.nm.user.app.v1.deliveryAddress.dto.{DeliveryAddressDto, DeliveryAddressQueryFilter}
import com.nm.user.app.v1.deliveryAddress.service.DeliverAddressService
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.util.Future

@Singleton
class DeliverAddressController @Inject()(deliverAddressService: DeliverAddressService) extends Controller {

  prefix("/delivery_address") {

    get("/:uid/list") { request: Request =>
      val deliveryAddressDtos: Future[Seq[DeliveryAddressDto]] = deliverAddressService.selectAll(request.getIntParam("uid"))
      deliveryAddressDtos.onSuccess { x =>
        info(s"result is $x")
      }.onFailure(ex =>
        error("occur error", ex)
      )
      deliveryAddressDtos
    }

    post("/add") { request: DeliveryAddressDto =>
      deliverAddressService.save(request).onSuccess { _ =>
        response.created
      }
    }

    delete("/:id/remove") { request: Request =>
      deliverAddressService.delete(request.getIntParam("id")).onSuccess(_ => response.ok)
    }

    post("/:id/update") { request: DeliveryAddressDto =>
      deliverAddressService.updateByPrimaryKey(request).onSuccess { _ =>
        response.ok
      }
    }

    get("/list") { request: DeliveryAddressQueryFilter =>
      deliverAddressService.query(request)
    }

    get("/:id/info") { request: Request =>
      val id = request.getIntParam("id")
      val deliveryAddress: Future[Option[DeliveryAddress]] = deliverAddressService.selectByPrimaryKey(id)
      deliveryAddress.onSuccess { x =>
        response.ok
        info(s"success, $x")
      }.onFailure { x =>
        error(s"error, $x")
      }

    }
  }
}
