package com.nm.user.modules

import com.nm.user.dtab.Dtabs
import com.twitter.finagle.Dtab
import com.twitter.inject.TwitterModule

object DtabModules extends TwitterModule {

  val configFile = flag("dtab.env", "test", "dtab environment")

  val dtabFormat = "/zk#     => /%s"

  protected override def configure(): Unit = {
    val dtabStr = configFile() match {
      case "dev" => dtabFormat.format("dev")
      case "test" => dtabFormat.format("test")
      case "production" => dtabFormat.format("prod")
      case unknow => throw new RuntimeException(s"invalid dtab env config, $unknow")
    }

    val envDtab = Dtab.read(dtabStr)
    Dtabs.init(envDtab)
  }
}
