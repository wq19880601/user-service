package com.nm.user.modules

import javax.inject.Singleton

import com.github.racc.tscg.TypesafeConfigModule
import com.google.inject.Provides
import com.twitter.inject.{Logging, TwitterModule}
import com.typesafe.config.{Config, ConfigFactory}

object FinatraTypeSafeConfigModule extends TwitterModule with Logging {

  val configFile = flag("config.file", "dev", "application run mode [dev:default, alpha, sandbox, beta, real]")

  private lazy val config = {
    val specified = configFile()

    val config = if (specified.nonEmpty) {
      logger.info(s"LOADING SPECIFIED CONFIG FROM: ${specified}")
      ConfigFactory load ("application-" + specified)
    } else {
      logger.warn("LOADING DEFAULT CONFIG!")
      ConfigFactory.load()
    }

    info(s"current env = $specified")
    config
  }


  protected override def configure(): Unit = {
    install(TypesafeConfigModule.fromConfig(config))
  }


  @Provides
  @Singleton
  def provideConfig(): Config = {
    config
  }
}
