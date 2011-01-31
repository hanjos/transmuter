package com.googlecode.transmuter.fixture;

import com.googlecode.transmuter.converter.Converts;

import java.io.Serializable;
import java.util.List;


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