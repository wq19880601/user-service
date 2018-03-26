package com.nm.user.common.domain

sealed abstract class MessageDto {
  val errorCode: Int
  val msg: String = ""

}


object MessageDto {

  class ErrorResponse(errorMsg: String) extends MessageDto {

    override val msg: String = errorMsg
    override val errorCode: Int = -1
  }

  class SuccessReponse(body:String) extends MessageDto {
    override val errorCode: Int = 1
  }

}



