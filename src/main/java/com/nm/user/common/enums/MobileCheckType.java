package com.nm.user.common.enums;

public enum MobileCheckType {
  REGISTER("register"), FORGET_PASSWD("forget");

  private String type;

  MobileCheckType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
