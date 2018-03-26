package com.nm.user.common.utils

import com.google.common.hash.Hashing

object HashingUtils {

  val mask = "JoBDKOVNLrOw7dwku8LZq1rgUm3pFMDabClXIGLAaxLddTWnbr"

  def md5(content:String):String = {
    Hashing.md5().hashBytes(content.getBytes).toString
  }


  def md5ByMask(content:String) = {
    Hashing.md5().hashBytes((mask + content).getBytes).toString
  }

  def main(args: Array[String]): Unit = {
    val str = md5ByMask("123456789")
    println(str)
  }

}
