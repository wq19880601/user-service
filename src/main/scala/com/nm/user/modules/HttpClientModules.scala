package com.nm.user.modules

import javax.inject.Singleton

import com.google.inject.Provides
import com.nm.user.annotations.SmsGatewayClient
import com.twitter.finagle.http.Response
import com.twitter.finagle.service.RetryPolicy
import com.twitter.finatra.http.response.ResponseUtils
import com.twitter.finatra.httpclient.{HttpClient, RichHttpClient}
import com.twitter.finatra.json.FinatraObjectMapper
import com.twitter.inject.TwitterModule
import com.twitter.inject.conversions.time._
import com.twitter.inject.utils.RetryPolicyUtils.constantRetry
import com.twitter.util.Try
import com.typesafe.config.Config

object HttpClientModules extends TwitterModule {

  val httpModules = Seq(SmsModule)


  abstract class AbstractHttpClient {

    def dest: String

    def retryPolicy: Option[RetryPolicy[Try[Response]]] = None

    def get(mapper: FinatraObjectMapper): HttpClient = {

      val httpService = RichHttpClient.newClientService(dest = dest)
      val httpClient = new HttpClient(
        httpService = httpService,
        retryPolicy = retryPolicy,
        mapper = mapper
      )
      httpClient

    }
  }


  object SmsModule extends TwitterModule {
    @Singleton
    @Provides
    @SmsGatewayClient
    def smsHttpClient(config: Config, mapper: FinatraObjectMapper): HttpClient = {
      val httpClient = new AbstractHttpClient {
        override def dest: String = config.getString("client.smsGateway.host")

        override def retryPolicy: Option[RetryPolicy[Try[Response]]] = Some(
          constantRetry(
            start = 10.millis,
            numRetries = 3,
            shouldRetry = ResponseUtils.Http4xxOr5xxResponses
          )
        )
      }
      httpClient.get(mapper)
    }

  }


}





