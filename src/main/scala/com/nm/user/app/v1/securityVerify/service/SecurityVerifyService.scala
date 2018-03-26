package com.nm.user.app.v1.securityVerify.service

import java.awt.Color
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.concurrent.Executors
import javax.inject.Inject

import com.github.racc.tscg.TypesafeConfig
import com.nm.user.app.v1.notify.NotifyService
import com.nm.user.app.v1.securityVerify.dto.{ImageCodeDto, SmsCodeGetDto}
import com.nm.user.app.v1.userCenter.dao.UserInfoRepository
import com.nm.user.common.exceptions.BusinessException
import com.nm.user.common.utils.HashingUtils
import com.nm.user.modules.RedisModule.RedisClient
import com.twitter.conversions.time._
import com.twitter.inject.Logging
import com.twitter.io.{Buf, Bufs}
import com.twitter.util.{Future, FuturePool}
import org.apache.commons.codec.binary.Base64
import org.patchca.color.SingleColorFactory
import org.patchca.filter.predefined.CurvesRippleFilterFactory
import org.patchca.service.ConfigurableCaptchaService
import org.patchca.utils.encoder.EncoderHelper

import scala.util.Random

object SecurityVerifyService {
  val mobile_verify_failed = "%s_sms_register_fail_count"

}

class SecurityVerifyService @Inject()(val redisClient: RedisClient, val notifyService: NotifyService, val userInfoRepository: UserInfoRepository,
                                      @TypesafeConfig("mobile.register.error.smsCode.times") val errorTimes: Int) extends Logging {

  import SecurityVerifyService._

  lazy val executors = Executors.newFixedThreadPool(10)

  val cs = new ConfigurableCaptchaService
  cs.setColorFactory(new SingleColorFactory(new Color(25, 60, 170)))
  cs.setFilterFactory(new CurvesRippleFilterFactory(cs.getColorFactory))


  def getImageCode(): Future[ImageCodeDto] = {

    val futurePool = FuturePool.apply(executors)

    cs.getWordFactory.getNextWord()
    val bos = new ByteArrayOutputStream()
    val generatedFileF: Future[(String, String)] = futurePool {
      val randomText = EncoderHelper.getChallangeAndWriteImage(cs, "png", bos)
      info(s"imagecode=$randomText}")

      val base64EncodedContent = Base64.encodeBase64String(bos.toByteArray)
      (randomText, base64EncodedContent)
    }.ensure {
      bos.close()
    }
    val md5Code = HashingUtils.md5(UUID.randomUUID().toString)
    val result: Future[ImageCodeDto] = for {
      (text, baseContent) <- generatedFileF
      _ <- redisClient.setPx(Buf.Utf8(md5Code), 10.minutes.inMilliseconds, Buf.Utf8(text))
    } yield ImageCodeDto(md5Code, baseContent)

    result
  }

  /**
    *
    *
    * @param mobile
    * @return
    */
  def doCheck(mobile: Long): Future[SmsCodeGetDto] = {
    if (Option(mobile).isEmpty) Future.exception(BusinessException("手机号非法"))
    else {
      val result = userInfoRepository.findUserInfoByMobile(mobile).flatMap {
        case Some(_) =>
          Future.exception(BusinessException("该手机号已经注册"))
        case None =>
          val redisKey = Buf.Utf8(mobile_verify_failed.format(mobile))
          val tooManyTimesF: Future[Boolean] = checkErrorTimes(redisKey)

          val checkResult = tooManyTimesF.map { checkValue =>
            if (checkValue) throw BusinessException("失败次数太多，请10分钟后在试")
            val randomCode = Random.shuffle(0 to 9).mkString("")

            val md5Sign: String = HashingUtils.md5(UUID.randomUUID().toString)
            redisClient.setPx(Buf.Utf8(s"${mobile}_${md5Sign}"), 30.minutes.inMilliseconds, Buf.Utf8(randomCode))
            notifyService.sendSmsMsg(Seq(mobile), randomCode)

            SmsCodeGetDto(smsVerifySign = Some(md5Sign))
          }.rescue {
            case e: BusinessException => Future.exception(e)
            case ex => {
              logger.error(s"do check occur error, mobile=$mobile", ex)
              Future.exception(BusinessException("系统异常"))
            }
          }
          checkResult
      }
      result
    }
  }

  private def checkErrorTimes(redisKey: Buf) = {
    val tooManyTimesF: Future[Boolean] = redisClient.get(redisKey).map {
      case None => false
      case Some(Buf.Utf8(num)) =>
        if (num.toInt > errorTimes) {
          redisClient.expire(redisKey, 10.minutes.inLongSeconds)
          true
        } else false
    }
    tooManyTimesF
  }

  def verifyImageCode(verifySign: String, imageCode: String): Future[Boolean] = {
    val verifyResultF = for {
      Some(Buf.Utf8(code)) <- redisClient.get(Bufs.utf8Buf(verifySign))
    } yield (if (code == imageCode) true else false)


    verifyResultF
  }


  def verifySmsCode(mobile: Long, smsCode: String, verifySign: String): Future[Boolean] = {
    val errorTimes_key = Buf.Utf8(mobile_verify_failed.format(mobile))

    val verifyErrorTimesF: Future[Boolean] = checkErrorTimes(errorTimes_key)
    val result: Future[Boolean] = verifyErrorTimesF.flatMap { result =>
      if (result) Future.exception(BusinessException("失败次数太多，请10分钟稍后重试"))
      else {
        val verifyResult = redisClient.get(Buf.Utf8(s"${mobile}_${verifySign}")).flatMap {
          case Some(Buf.Utf8(code)) => {
            if (code == smsCode) Future.value(true)
            else {
              redisClient.incr(errorTimes_key)
              Future.value(false)
            }
          }
          case None => Future.exception(BusinessException("短信已经过期"))
        }
        verifyResult
      }
    }
    result
  }


}
