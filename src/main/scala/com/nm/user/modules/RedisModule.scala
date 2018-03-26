package com.nm.user.modules


import javax.inject.Singleton

import com.github.racc.tscg.TypesafeConfig
import com.google.inject.Provides
import com.twitter.finagle.redis.SentinelClient
import com.twitter.finagle.{Redis, redis}
import com.twitter.inject.{Logging, TwitterModule}
import com.twitter.io.Bufs
import com.twitter.util.{Await, Future}

object RedisModule extends TwitterModule with Logging {

  type RedisClient = redis.Client

  @Provides
  @Singleton
  def providerRedisClient(@TypesafeConfig("redis.sentinal") sentinal: String,
                          @TypesafeConfig("redis.master.main.name") masterName: String,
                          @TypesafeConfig("redis.main.password") authPasswd: String): RedisClient = {

    val sentinelClient = Redis.newSentinelClient(sentinal)

    val masterNode: Future[SentinelClient.MasterNode] = sentinelClient.master(masterName).map { masterNode =>
      if (masterNode == null || masterNode.ip.isEmpty)
        throw new RuntimeException(s"no master found,master_name=$masterName")

      logger.info(s"master find,ip=${masterNode.ip}")
      masterNode
    }

    val redisClientF = masterNode.map { node =>
      info(s"master found, ip=${node.ip}, port=${node.port}")
      val client = Redis.newRichClient(s"${node.ip}:${node.port}")
      val authFailedContent = s"redis auth failed,passwd=$authPasswd}"
      client.auth(Bufs.utf8Buf(authPasswd)).onFailure(ex =>
        throw new RuntimeException(authFailedContent, ex)
      ).onSuccess{_ =>
        info(s"redis auth success, passwd=$authPasswd")
      }
      client
    }
    redisClientF.onFailure { ex =>
      error("redis start error", ex)
      throw ex;
    }
    val redisClient = Await.result(redisClientF)

    info("redis start success")
    redisClient
  }

}
