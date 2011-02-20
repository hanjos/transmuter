package com.googlecode.transmuter.fixture;

import com.googlecode.transmuter.converter.Converts;

public class StringConverter {
  @Converts
  public String stringify(Object object) {
    return String.valueOf(object);
  }
  
  @Override
  public boolean equals(Object o) {
    return o instanceof StringConverter;
  }
  
  @Override
  public String toString() {
   return "StringConverter!"; 
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}