package com.googlecode.transmuter.fixture;

import java.io.Serializable;
import java.util.List;

import com.googlecode.transmuter.converter.Converts;


public class MultipleValidConverter {
  @Converts
  public String toString(List<String> l) {
    return String.valueOf(l);
  }
  
  @Converts
  public String toString(Serializable s) {
    return String.valueOf(s);
  }
}