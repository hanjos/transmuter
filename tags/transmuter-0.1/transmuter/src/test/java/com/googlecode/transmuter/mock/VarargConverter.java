package com.googlecode.transmuter.mock;

import com.googlecode.transmuter.Converts;

public final class VarargConverter {
  @Converts
  public String stringifyArray(Object... o) {
    return String.valueOf(o);
  }
}