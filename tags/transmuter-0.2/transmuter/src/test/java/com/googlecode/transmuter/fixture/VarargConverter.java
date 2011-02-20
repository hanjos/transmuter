package com.googlecode.transmuter.fixture;

import com.googlecode.transmuter.converter.Converts;

public final class VarargConverter {
  @Converts
  public String stringifyArray(Object... o) {
    return String.valueOf(o);
  }
}