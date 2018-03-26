package com.nm.user.app.v1.userCenter.service

import java.util.{Date, UUID}
import javax.inject.{Inject, Singleton}

import com.nm.user.app.v1.securityVerify.dto.{MobileRegisterRepDto, MobileRegisterReqDto}
import com.nm.user.app.v1.securityVerify.service.SecurityVerifyService
import com.nm.user.app.v1.userCenter.dao.UserInfoRepository
import com.nm.user.app.v1.userCenter.dto._
import com.nm.user.common.enums.{FrozenStatus, MobileCheckType}
import com.nm.user.common.exceptions.BusinessException
import com.nm.user.common.utils.{HashingUtils, JwtUtils}
import com.nm.user.modules.RedisModule.RedisClient
import com.twitter.conversions.time._
import com.twitter.inject.Logging
import com.twitter.io.Buf
import com.twitter.util.Future
import org.joda.time.{DateTime, LocalDate}


@Singleton
class UserCenterService @Inject()(val redisClient: RedisClient,
                                  val userInfoRepository: UserInfoRepository, val securityVerifyService: SecurityVerifyService) extends Logging {

  val mobile_login_fail_count = "%s_mobile_login_fail_count"

  def getToken(uid: String): UserTokenDto = {
    val token = JwtUtils.getToken(uid)
    val now = new Date()
    val dueDate = LocalDate.fromDateFields(now).plusDays(20).toDate

    val userTokenDto = UserTokenDto(id = -1, uid = uid, token = token, expiration = Some(dueDate), createTime = now, modifyTime = now)
    userTokenDto
  }

  def findUserByUid(uid: String): Future[Option[UserInfoDto]] = {
    userInfoRepository.findUserInfoByUid(uid)
  }

  def authToken(uid: String, token: String): Future[UserInfoDto] = {
    val tokenParam = JwtUtils.validateToken(token)
    if (!tokenParam) {
      Future.exception(BusinessException("invalid token"))
    } else {
      val value: Future[Future[UserInfoDto]] = for {
        Some(user) <- userInfoRepository.findUserInfoByUid(uid)
        Some(tokenInfo) <- userInfoRepository.findUserTokenInfo(uid)
      } yield {
        if (tokenInfo.token == token) {
          if (!JwtUtils.isNotExpiration(tokenInfo.token)) {
            Future.exception(BusinessException("token is expired"))
          } else Future.value(user)
        } else {
          Future.exception(BusinessException("invalid token"))
        }
      }
      value.flatten
    }
  }

  def registerByMobile(mobileRegisterDto: MobileRegisterReqDto): Future[MobileRegisterRepDto] = {
    val verifySmsF: Future[Boolean] = securityVerifyService.verifySmsCode(mobileRegisterDto.mobile, mobileRegisterDto.smsCode, mobileRegisterDto.verifySign)
    val saveResultF: Future[Long] = verifySmsF.flatMap {
      verifySms =>
        if (!verifySms) {
          Future.exception(BusinessException("验证码不正确"))
        } else {
          val existsUserInfoF: Future[Option[UserInfoDto]] = userInfoRepository.findUserInfoByMobile(mobileRegisterDto.mobile)

          val mobileCheckTypeOpt: Option[MobileCheckType] = MobileCheckType.values().find(_.getType == mobileRegisterDto.mobileCheckType)
          val updateResult: Future[Long] = mobileCheckTypeOpt match {
            case Some(MobileCheckType.REGISTER) =>
              val value: Future[Long] = existsUserInfoF.flatMap {
                case Some(user) =>
                  info(s"mobile register, user already exists in the system, mobile=${
                    user.mobile
                  }")
                  Future.exception(BusinessException("该用户已经注册"))
                case None =>
                  val uid = UUID.randomUUID().toString

                  val passwd= HashingUtils.md5ByMask(mobileRegisterDto.password)
                  val userInfoDto = UserInfoDto(id = -1, uid = uid,
                    uName = mobileRegisterDto.mobile.toString, uPass = Some(passwd), mobile = Option(mobileRegisterDto.mobile))

                  val value: Future[Long] = userInfoRepository.save(userInfoDto, getToken(uid), None).onFailure {
                    ex =>
                      error(s"save user info occur error,mobile=${
                        mobileRegisterDto.mobile
                      }", ex)
                  }
                  value
              }
              value
            case Some(MobileCheckType.FORGET_PASSWD) =>
              val value = existsUserInfoF.flatMap {
                case None =>
                  Future.exception(BusinessException("用户不存在"))
                case Some(userInfo) =>
                  val token = JwtUtils.getToken(userInfo.uid)
                  val dueDate = DateTime.now().plusDays(20).toDate
                  val updateResult = userInfoRepository.updatePasswdToken(userInfo.uid, mobileRegisterDto.password, token, dueDate)
                  updateResult
              }
              value
            case _ => Future.exception(BusinessException("类型非法"))
          }
          updateResult


        }
    }
    val result = saveResultF.map(_ => MobileRegisterRepDto(1)).onFailure {
      ex =>
        error(s"register by mobile occur error,mobile=${
          mobileRegisterDto.mobile
        }", ex)
    }.rescue {
      case ex: BusinessException =>
        Future.exception(ex)
      case _ =>
        Future.exception(BusinessException("服务器异常"))
    }
    result
  }


  def registerByTp(userTpSourceDto: UserTpSourceDto): Future[Long] = {
    val token = JwtUtils.getToken(userTpSourceDto.tpUid)

    val uid = UUID.randomUUID().toString
    val now = new Date()

    val registerResult: Future[Long] = userInfoRepository.findUserTpSource(userTpSourceDto.sourceType, userTpSourceDto.tpUid).flatMap {
      case Some(tpSource) =>
        val userInfoDto = UserInfoDto(-1, uid = tpSource.uid, nick = Option(userTpSourceDto.nick),
          iconUrl = userTpSourceDto.iconUrl, uName = userTpSourceDto.account)
        val userToken = UserTokenDto(-1, uid = tpSource.uid, token = token, createTime = now, modifyTime = now)

        val saveResult: Future[Long] = userInfoRepository.save(userInfoDto, userToken, Some(userTpSourceDto))
        saveResult
      case None =>
        val userInfoDto = UserInfoDto(id = -1, uid = uid, nick = Option(userTpSourceDto.nick),
          iconUrl = userTpSourceDto.iconUrl, uName = userTpSourceDto.account)
        val userToken = UserTokenDto(-1, uid, token = token, createTime = now, modifyTime = now)

        val saveResult: Future[Long] = userInfoRepository.save(userInfoDto, userToken, Some(userTpSourceDto))
        saveResult
    }
    registerResult
  }

  def logout(uid: String): Future[Boolean] = {
    userInfoRepository.findUserInfoByUid(uid).flatMap {
      case Some(user) =>
        val token = JwtUtils.getToken(uid, 1)
        val result = userInfoRepository.updateToken(uid, token).flatMap {
          _ =>
            Future.value(true)
        }.rescue {
          case ex =>
            error(s"update token occur error, uid=$uid", ex)
            Future.exception(BusinessException("系统异常"))
        }
        result
      case None =>
        Future.exception(BusinessException("用户不存在"))
    }
  }

  def login(mobile: Long, passwd: String): Future[UserLoginResponseDto] = {

    val loginFailCntRedsKey = Buf.Utf8(mobile_login_fail_count.format(mobile))
    val destPasswd = HashingUtils.md5ByMask(passwd)
    val result = redisClient.get(loginFailCntRedsKey).flatMap {
      case Some(Buf.Utf8(failedTimes)) =>
        if (failedTimes.toInt > 10)
          Future.exception(BusinessException("登录失败次数太多，请稍后再试"))
        else {
          loginByMobile(mobile, destPasswd, loginFailCntRedsKey)
        }
      case None =>
        loginByMobile(mobile, destPasswd, loginFailCntRedsKey)
    }
    result
  }


  private def loginByMobile(mobile: Long, passwd: String, redisKey: Buf): Future[UserLoginResponseDto] = {
    val userInfoDtoF: Future[Option[UserInfoDto]] = userInfoRepository.findUserInfoByMobile(mobile)
    val userInfoExistsF = userInfoDtoF.flatMap {
      case Some(dto) => Future.value(dto)
      case None => Future.exception(BusinessException("用户不存在"))
    }
    val value: Future[UserLoginResponseDto] = userInfoExistsF.flatMap {
      user =>
        if (user.isFrozen == FrozenStatus.FROZEN.getStatus) {
          Future.exception(BusinessException("该用户被冻结，请联系客户"))
        } else {
          if (user.uPass.contains(passwd)) {
            val tokenResult = userInfoRepository.findUserTokenInfo(user.uid).flatMap {
              case None =>
                Future.exception(BusinessException("token 信息不存在"))
              case Some(_) =>
                val token = JwtUtils.getToken(user.uid)
                val expire = DateTime.now().plusDays(20)
                userInfoRepository.updateLastLoginTime(user.uid)
                userInfoRepository.updateToken(user.uid,token).map { _ =>
                  val ulr = UserLoginResponseDto(token, expire.toDate)
                  Future.value(ulr)
                }.rescue {
                  case ex =>
                    error(s"update user token occur error, ${user.uid}", ex)
                    Future.exception(BusinessException("update user info error"))
                }
            }
            tokenResult.flatten
          } else {
            val value = for {
              _ <- redisClient.incr(redisKey)
              Some(Buf.Utf8(errorTimes)) <- redisClient.get(redisKey)
            } yield {
              if (errorTimes.toInt == 10) {
                redisClient.expire(redisKey, 1.minute.inLongSeconds)
              }
              Future.exception(BusinessException("密码不正确，请重新登陆"))
            }
            value.flatten
          }
        }
    }
    value
  }

  def queryUserCenterByUid(uid: String): Future[UserCenterDto] = {
    userInfoRepository.findUserCenterByUid(uid)
  }
}
