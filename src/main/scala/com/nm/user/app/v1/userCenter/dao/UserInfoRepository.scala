package com.nm.user.app.v1.userCenter.dao

import java.util.Date
import javax.inject.Inject

import com.nm.user.app.v1.userCenter.dto
import com.nm.user.app.v1.userCenter.dto.{UserCenterDto, UserInfoDto, UserTokenDto, UserTpSourceDto}
import com.nm.user.common.enums.FrozenStatus
import com.nm.user.core.quill.{QuillExtensions, TableSchema}
import com.nm.user.modules.QuillDatabaseModule.QuillDatabaseSource
import com.twitter.inject.Logging
import com.twitter.util.Future

import scala.collection.immutable

class UserInfoRepository @Inject()(val ctx: QuillDatabaseSource) extends QuillExtensions with TableSchema with Logging {

  import ctx._


  def save(userInfoDto: UserInfoDto, userTokenDto: UserTokenDto, userTpSourceDtoOpt: Option[UserTpSourceDto]): Future[Long] = {
    implicit val userInfoInsertMeta = insertMeta[UserInfo](_.id, _.createTime, _.modifyTime)
    implicit val userTokenInsertMeta = insertMeta[UserToken](_.id, _.createTime, _.modifyTime, _.isDeleted)
    implicit val userTpSourceInsertMeta = insertMeta[UserTpSource](_.id, _.createTime, _.modifyTime)

    val now = getCurrentDate()


    ctx.transaction {
      ctx.run {
        val userInfo = UserInfo(id = -1, uid = userInfoDto.uid, nick = userInfoDto.nick, iconUrl = userInfoDto.iconUrl, uName = userInfoDto.uName,
          mobile = userInfoDto.mobile, gender = userInfoDto.gender, uPass = userInfoDto.uPass, email = userInfoDto.email,
          birthDay = userInfoDto.birthDay, appVersion = userInfoDto.appVersion, createTime = Some(now), modifyTime = now)
        quote {
          userInfoTable.insert(lift(userInfo))
        }
      }
      for {
        userTpSourceDto <- userTpSourceDtoOpt
      } yield {
        val userTpSource = UserTpSource(id = -1, account = userTpSourceDto.account, nick = userTpSourceDto.nick, uid = userTpSourceDto.uid, sourceType = userTpSourceDto.sourceType,
          openId = userTpSourceDto.openId, iconUrl = userTpSourceDto.iconUrl, tpUid = userTpSourceDto.tpUid, gender = userTpSourceDto.gender,
          accessToken = userTpSourceDto.accessToken, refreshToken = userTpSourceDto.refreshToken, expiration = userTpSourceDto.expiration,
          createTime = Some(now), modifyTime = now)
        ctx.run(quote {
          userTpSourceTable.insert(lift(userTpSource))
        })
      }

      ctx.run {
        val userToken = UserToken(id = -1, deviceId = userTokenDto.deviceId, token = userTokenDto.token, refreshToken = userTokenDto.refreshToken,
          expiration = userTokenDto.expiration, createTime = getCurrentDate(), modifyTime = getCurrentDate(), uid = userTokenDto.uid)
        quote {
          userTokenTable.insert(lift(userToken))
        }
      }
    }

  }

  def updatePasswdToken(uid: String, passwd: String, token: String, dueDate: Date): Future[Long] = {
    val now = getCurrentDate()
    ctx.transaction {
      ctx.run {
        quote {
          userInfoTable.filter(_.uid == lift(uid)).update(u => u.modifyTime -> lift(now), u => u.uPass -> lift(Option(passwd)))
        }
      }
      ctx.run {
        quote {
          userTokenTable.filter(_.uid == lift(uid)).update(ut => ut.modifyTime -> lift(now), ut => ut.token -> lift(token), ut => ut.expiration -> lift(Option(dueDate)))
        }
      }
    }
  }

  def updateToken(uid: String, token: String): Future[Long] = {
    val now = getCurrentDate()
    ctx.run {
      quote {
        userTokenTable.filter(_.uid == lift(uid)).update(ut => ut.modifyTime -> lift(now), ut => token -> lift(token), ut => ut.expiration -> lift(Option(now)))
      }
    }
  }

  def frozen(uid: String, frozenStatus: FrozenStatus): Future[Long] = {
    val frozenResult = ctx.run {
      quote {
        userInfoTable.filter(_.uid == lift(uid)).update(u => (u.modifyTime -> lift(getCurrentDate())), u => (u.isFrozen -> lift(frozenStatus.getStatus.toByte)))
      }
    }
    frozenResult
  }

  def update(userInfoDto: UserInfoDto, userTokenDto: UserTokenDto, userTpSourceDto: UserTpSourceDto): Future[Long] = {
    val userInfo = userDto2Info(userInfoDto)
    val userTpSource = UserTpDto2Info(userTpSourceDto)
    ctx.transaction {
      ctx.run {
        quote {
          query[UserInfo].filter(_.uid == lift(userInfoDto.uid)).update(lift(userInfo))
        }
      }
      ctx.run {
        quote {
          query[UserToken].filter(_.uid == lift(userInfoDto.uid)).update(u => u.token -> lift(userTokenDto.token), u => u.modifyTime -> lift(new Date()))
        }
      }
      ctx.run {
        quote {
          query[UserTpSource].filter(_.uid == lift(userInfoDto.uid)).update(lift(userTpSource))
        }
      }
    }
  }

  def updateLastLoginTime(uid: String): Future[Long] = {
    ctx.run {
      quote {
        userInfoTable.filter(_.uid == lift(uid)).update(f => f.lastLoginTime -> lift(Option(new Date())))
      }
    }
  }

  def findUserTokenInfo(uid: String): Future[Option[UserTokenDto]] = {
    val token = ctx.run {
      quote {
        userTokenTable.filter(_.uid == lift(uid))
      }
    }.map(_.headOption.map(userToken2Dto))

    token
  }


  def findUserInfoByUid(uid: String): Future[Option[UserInfoDto]] = {
    val result = ctx.run {
      quote {
        userInfoTable.filter(_.uid == lift(uid))
      }
    }.map(_.headOption.map(userInfo2Dto))
    result
  }


  def findUserInfoByMobile(mobile: Long): Future[Option[UserInfoDto]] = {
    val result = ctx.run {
      quote {
        userInfoTable.filter(_.mobile.exists(_ == lift(mobile)))
      }
    }.map(_.headOption.map(userInfo2Dto))

    result
  }

  def findUserCenterByUid(uid: String): Future[UserCenterDto] = {
    val userInfoF: Future[Option[UserInfoDto]] = ctx.run {
      quote {
        query[UserInfo].filter(_.uid == lift(uid))
      }
    }.map(_.headOption.map(userInfo2Dto))

    val userTokenF: Future[Option[UserTokenDto]] = ctx.run {
      quote {
        query[UserToken].filter(_.uid == lift(uid))
      }
    }.map(_.headOption.map(userToken2Dto))

    val userTpSourceSeqF: Future[immutable.Seq[UserTpSourceDto]] = ctx.run {
      quote {
        query[UserTpSource].filter(_.uid == lift(uid))
      }
    }.map(_.map(userTpSource2Dto))

    val userCenterDto: Future[UserCenterDto] = for {
      userInfo <- userInfoF
      userToken <- userTokenF
      userTpSources <- userTpSourceSeqF
    } yield {
      dto.UserCenterDto(userInfo, userToken, userTpSources)
    }
    userCenterDto
  }


  def findUserTpSource(sourceType: Int, tpUid: String): Future[Option[UserTpSource]] = {
    val userTpSourceDto: Future[Option[UserTpSource]] = ctx.run {
      quote {
        query[UserTpSource].filter(tp => tp.tpUid == lift(tpUid) && tp.sourceType == lift(sourceType))
      }
    }.map(_.headOption)
    userTpSourceDto
  }


  private def userDto2Info(info: UserInfoDto): UserInfo = {
    val now = new Date()
    val userInfo = UserInfo(id = info.id, nick = info.nick, uid = info.uid, iconUrl = info.iconUrl, uName = info.uName,
      uPass = info.uPass, mobile = info.mobile, gender = info.gender, email = info.email,
      birthDay = info.birthDay, appVersion = info.appVersion, modifyTime = now)
    userInfo
  }

  private def UserTpDto2Info(tpSource: UserTpSourceDto): UserTpSource = {
    val tpSourceDto = UserTpSource(id = tpSource.id, uid = tpSource.uid, account = tpSource.account, sourceType = tpSource.sourceType, nick = tpSource.nick,
      tpUid = tpSource.tpUid, openId = tpSource.openId, iconUrl = tpSource.iconUrl, gender = tpSource.gender, accessToken = tpSource.accessToken,
      refreshToken = tpSource.refreshToken, expiration = tpSource.expiration, modifyTime = tpSource.modifyTime)
    tpSourceDto
  }


  private def userInfo2Dto(info: UserInfo): UserInfoDto = {
    val userInfoDto = UserInfoDto(id = info.id, nick = info.nick, uid = info.uid, iconUrl = info.iconUrl, uName = info.uName,
      uPass = info.uPass, mobile = info.mobile, gender = info.gender, email = info.email,
      birthDay = info.birthDay, appVersion = info.appVersion, isFrozen = info.isFrozen)
    userInfoDto
  }

  private def userToken2Dto(token: UserToken): UserTokenDto = {
    val userTokenDto = UserTokenDto(id = token.id, uid = token.uid, deviceId = token.deviceId,
      token = token.token, createTime = token.createTime, modifyTime = token.modifyTime)
    userTokenDto
  }


  private def userTpSource2Dto(tpSource: UserTpSource): UserTpSourceDto = {
    val tpSourceDto = UserTpSourceDto(id = tpSource.id, uid = tpSource.uid, account = tpSource.account, sourceType = tpSource.sourceType, nick = tpSource.nick,
      tpUid = tpSource.tpUid, openId = tpSource.openId, iconUrl = tpSource.iconUrl, gender = tpSource.gender, accessToken = tpSource.accessToken,
      refreshToken = tpSource.refreshToken, expiration = tpSource.expiration, modifyTime = tpSource.modifyTime)
    tpSourceDto
  }


}
