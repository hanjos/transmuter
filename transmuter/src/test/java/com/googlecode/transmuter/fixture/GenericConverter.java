package com.googlecode.transmuter.fixture;

import com.googlecode.transmuter.Converts;

public class GenericConverter<From, To> {
  @SuppressWarnings("unchecked")
  @Converts
  public To convert(From from) {
    return (To) from;
  }
}