package com.nm.user.dtab

import com.twitter.finagle.Dtab
import com.twitter.inject.Logging

import scala.io.Source

object Dtabs extends Logging{

  def init(additionalDtab: Dtab = Dtab.empty): Unit = {
    Dtab.base = base ++ additionalDtab

    info(s"dtab config,${Dtab.base}")
  }


  /**
    * /$/com.twitter.serverset/127.0.0.1:2181/finatra/http/service
    *
    * base dtab to resolve requests on your local system
    */
  val base = {
    val dtabFile = Thread.currentThread().getContextClassLoader.getResource("upsource.dtab")
    val dtabs = Source.fromURL(dtabFile).mkString
    Dtab.read(dtabs)
  }
}
