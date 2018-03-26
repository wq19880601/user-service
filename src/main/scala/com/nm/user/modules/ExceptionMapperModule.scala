package com.nm.user.modules

import com.nm.user.core.finatra.BusinessExceptionMapper
import com.twitter.finatra.http.exceptions.ExceptionManager
import com.twitter.inject.{Injector, TwitterModule}

object ExceptionMapperModule extends  TwitterModule{

  override def singletonStartup(injector: Injector): Unit = {
    val manager = injector.instance[ExceptionManager]
    manager.add[BusinessExceptionMapper]
  }

}
