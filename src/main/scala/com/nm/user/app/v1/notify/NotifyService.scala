package com.nm.user.app.v1.notify

import java.net.URLEncoder
import javax.inject.{Inject, Singleton}

import com.nm.user.annotations.SmsGatewayClient
import com.nm.user.modules.RedisModule.RedisClient
import com.twitter.finatra.http.response.ResponseUtils
import com.twitter.finatra.httpclient.{HttpClient, RequestBuilder}
import com.twitter.inject.Logging
import com.twitter.io.Buf.Utf8
import com.twitter.util.Future

@Singleton
class NotifyService @Inject()(redisClient: RedisClient, @SmsGatewayClient val smsGatewayClient: HttpClient) extends Logging {

  def sendSmsMsg(mobile: Seq[Long], msgContent: String): Future[Unit] = {
    info(s"send smg msg, mobile=${mobile.mkString(",")}, msgContent=$msgContent")
    val suffixUrl = "/MWGate/wmgw.asmx/MongateSendSubmit?userId=J02338&password=522165&pszMobis=%s&iMobiCount=%s&pszSubPort=*&MsgId=0&pszMsg=%s"
    val url = suffixUrl.format(mobile.mkString(","), mobile.size, URLEncoder.encode(msgContent, "utf-8"))
//    val req = RequestBuilder.get(url)
//    smsGatewayClient.execute(req).onFailure(ex => {
//      error(s"send message by gateway occur error,mobile=$mobile", ex)
//    }).onSuccess(rep => {
//      if (ResponseUtils.is4xxOr5xxResponse(rep)) {
//        error(s"sms gateway failed,mobile=$mobile,reponse_code=${rep.statusCode}")
//      }
//    }).unit
    Future.Unit
  }
}
