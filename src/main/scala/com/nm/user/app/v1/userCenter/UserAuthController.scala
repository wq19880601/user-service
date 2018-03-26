package com.nm.user.app.v1.userCenter

import javax.inject.{Inject, Singleton}

import com.nm.user.app.v1.securityVerify.dto.MobileRegisterReqDto
import com.nm.user.app.v1.userCenter.dto.{UserAuthTokenDto, UserMobileLoginDto, UserTpSourceDto}
import com.nm.user.app.v1.userCenter.service.UserCenterService
import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller

@Singleton
class UserAuthController @Inject()(val userCenterService: UserCenterService) extends Controller {

  prefix("/user") {
    post("/mobile/register") { mobileRegisterReqDto: MobileRegisterReqDto =>
      userCenterService.registerByMobile(mobileRegisterReqDto)
    }

    post("/mobile/login") { userMobileLoginDto: UserMobileLoginDto =>
      userCenterService.login(userMobileLoginDto.mobile, userMobileLoginDto.passwd)
    }

    post("/logout") { request: Request =>
      val uid = request.getParam("uid")
      userCenterService.logout(uid)
    }

    get("/getUserByUid") { request: Request =>
      val uid = request.getParam("uid")
      userCenterService.findUserByUid(uid)
    }

    post("/authToken") { request: UserAuthTokenDto =>
      userCenterService.authToken(request.uid,request.token)
    }

    post("/tp/register") { userTpSourceDto: UserTpSourceDto =>
      userCenterService.registerByTp(userTpSourceDto)
    }

    get("/query_info/:uid") { request: Request =>
      val uid = request.getParam("uid")
      userCenterService.queryUserCenterByUid(uid)
    }
  }

}
