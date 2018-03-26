package user.service

import com.nm.user.app.v1.deliveryAddress.dao.{DeliveryAddress, DeliveryAddressRepository}
import com.nm.user.app.v1.deliveryAddress.dto.DeliveryAddressQueryFilter
import com.nm.user.common.domain.PageRequest
import com.nm.user.modules.{FinatraTypeSafeConfigModule, QuillDatabaseModule}
import com.twitter.finatra.json.modules.FinatraJacksonModule
import com.twitter.inject.Test
import com.twitter.inject.app.TestInjector
import com.twitter.util.Await


class DeliverAddressRepositoryTest extends Test {

  val injector = TestInjector(modules = (Seq(FinatraJacksonModule, FinatraTypeSafeConfigModule,QuillDatabaseModule))).create
  val deliveryRepository = injector.instance[DeliveryAddressRepository]


  test("query by filter"){

    val filter = DeliveryAddressQueryFilter(cityId = Some(1), province = Some(1), receiveUser = Some("aaa"),pageRequest =  PageRequest(1,10))

    val result = deliveryRepository.query(filter)
    val addresses: Seq[DeliveryAddress] = Await.result(result)
    assert(addresses.nonEmpty)
    assert(addresses.size == 1)
  }

}
