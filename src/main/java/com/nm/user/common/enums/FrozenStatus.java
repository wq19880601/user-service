package com.nm.user.common.enums;

public enum FrozenStatus {
  FROZEN(1),UNFROZEN(0);

  private int status;

   FrozenStatus(int status) {
    this.status = status;
  }

  public int getStatus() {
    return status;
  }
}
