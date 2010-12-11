package com.googlecode.transmuter.fixture;

import com.googlecode.transmuter.Converts;

public final class GenericMethodConverter {
  @Converts
  public <T> T nonNull(T o) {
    if(o == null)
      throw new IllegalArgumentException();
    
    return o;
  }
}