package com.nm.user.common.domain

object PageRequest{
  val maxPageSize = 100
}
case class PageRequest(currentPage: Int = 1, pageSize: Int = 50) {
  import PageRequest._


  def getOffset(): Int = {
    if (pageSize > maxPageSize) {
      throw new RuntimeException(s"page size exceed , $maxPageSize ")
    }

    (currentPage -1) * pageSize
  }
}
