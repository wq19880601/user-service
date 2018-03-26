package com.nm.user.common.utils

import java.util.UUID

import com.twitter.conversions.time._
import pdi.jwt._


object JwtUtils {

  private val secret = "R8ENmB2RUTcWMMC3HC3B5S07iuSQLpEu2K9C9cxjDz6hJUpz32"

  private val hs: JwtAlgorithm.HS256.type = JwtAlgorithm.HS256

  def getToken(uid: String, expired:Long = 20.days.inMilliseconds): String = {
    val jwtEncode = Jwt.encode(JwtHeader(hs),
      JwtClaim().issuedNow.expiresAt(expired).by("walis_wang").to(uid).about("user_identify"), secret)
    jwtEncode.toString
  }

  def isNotExpiration(token: String): Boolean = {
    val result = Jwt.isValid(token, secret, Seq(hs), JwtOptions(notBefore = true))
    result
  }

  def validateToken(token: String): Boolean = {
    val result = Jwt.isValid(token, secret, Seq(hs))
    result
  }

  def main(args: Array[String]): Unit = {
    val token = getToken(UUID.randomUUID().toString)
    println(token)
    println(validateToken(token))

    val dbToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ3YWxpc193YW5nIiwic3ViIjoidXNlcl9pZGVudGlmeSIsImF1ZCI6WyJhNzVjMzFkNi0zZTA0LTRmNzItYTlmNS03OWMwZWQwNzY5ZTIiXSwiZXhwIjoxNzI4MDAwMDAwLCJpYXQiOjE1MTYzMzgyNjV9.2Q-FPpBqnaUN1sK1PHZNM5gRCu2w78aKG65PEGv7Kow"
    val result = validateToken(dbToken)
    println(s"validate token, $result")
    val expiration = isNotExpiration(dbToken)
    println(s"expiration, $expiration")

  }


}
