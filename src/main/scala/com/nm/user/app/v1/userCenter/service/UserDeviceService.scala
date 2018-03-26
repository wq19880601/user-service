package com.nm.user.app.v1.userCenter.service

import javax.inject.Inject

import com.nm.user.app.v1.userCenter.dao.UserDeviceRepository
import com.nm.user.app.v1.userCenter.dto.UserDeviceInfoDto
import com.twitter.util.Future

class UserDeviceService @Inject()(val userDeviceRepository: UserDeviceRepository) {

  def save(userDeviceInfoDto: UserDeviceInfoDto): Future[Long] = {
    userDeviceRepository.saveDeviceInfo(userDeviceInfoDto)
  }

}
