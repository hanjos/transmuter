package com.googlecode.transmuter.fixture;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.transmuter.converter.Converts;


public final class MultipleConverter {
  @Converts
  public String converter(double d) {
    return "double: " + d;
  }
  
  @SuppressWarnings("serial")
  @Converts
  public List<String> convert(final String s) {
    return new ArrayList<String>() {{ add(s); }};
  }
}