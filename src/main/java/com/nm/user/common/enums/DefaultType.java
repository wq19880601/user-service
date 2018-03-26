package com.nm.user.common.enums;

import java.util.Arrays;
import java.util.Optional;

public enum DefaultType {

  YES(1),NO(0);

  private int code;

  DefaultType(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public static DefaultType parse(int code){
    Optional<DefaultType> defaultTypeOptional = Arrays.stream(DefaultType.values()).filter(x -> x.code == code)
        .findFirst();
    if(!defaultTypeOptional.isPresent()){
      throw new RuntimeException("type not in the enum");
    }
    return defaultTypeOptional.get();
  }
}
