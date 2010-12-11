package com.googlecode.transmuter.fixture;

import com.googlecode.transmuter.Converts;


public class PartialGenericConverter<From> extends GenericConverter<From, String> {
  @Override
  @Converts
  public String convert(From from) {
    return String.valueOf(from);
  }
}