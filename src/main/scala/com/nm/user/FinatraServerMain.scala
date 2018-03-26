package com.nm.user

import com.nm.user.app.v1.deliveryAddress.DeliverAddressController
import com.nm.user.app.v1.securityVerify.SecurityVerifyController
import com.nm.user.app.v1.userCenter.UserAuthController
import com.nm.user.modules._
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import org.slf4j.Marker

object FinatraServerMain extends FinatraServer

class FinatraServer extends HttpServer {


  override def modules = Seq(
    FinatraTypeSafeConfigModule, QuillDatabaseModule, RedisModule, ExceptionMapperModule
  ) ++ HttpClientModules.httpModules

  override def jacksonModule = CustomJacksonModule


  def getDefaultAnnounceFormat(zkAdress: String) = s"zk!$zkAdress!/finatra/http/service/user!0"

  //  override protected def defaultHttpAnnouncement = {
  //    val zkaddr = "192.168.11.29:2181,192.168.11.32:2181,192.168.11.20:2181"
  //    getDefaultAnnounceFormat(zkaddr)
  //  }

  override protected def defaultHttpServerName: String = "user-center"


  override def defaultAdminPort = 9998

  override protected def disableAdminHttpServer: Boolean = super.disableAdminHttpServer

  override def defaultFinatraHttpPort = ":9999"


  override protected[this] def isTraceEnabled = super.isTraceEnabled

  override protected[this] def trace(marker: Marker, message: => Any): Unit = super.trace(marker, message)

  override def configureHttp(router: HttpRouter) {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add[DeliverAddressController]
      .add[SecurityVerifyController]
      .add[UserAuthController]
  }
}
