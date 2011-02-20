package com.googlecode.transmuter.fixture;

import com.googlecode.transmuter.converter.Converts;

public final class FlawedConverter {
  @Converts
  public boolean intraClassCollision1(int i) {
    return (i == 0) ? false : true;
  }
  
  @Converts
  public boolean intraClassCollision2(int i) {
    return i % 2 == 0;
  }
  
  /**
   * @param whatever asklj
   */
  @Converts
  public void voidAsReturnType(Object whatever) {
    // empty block
  }
  
  /**
   * @param a asd
   * @param b asd
   * @return something
   */
  @Converts
  public Object tooManyParameters(Object a, Object b) {
    return null;
  }
  
  @Converts
  public Object tooFewParameters() {
    return null;
  }
  
  @Converts
  public String extraClassCollision(double d) {
    return String.valueOf(d);
  }
  
  /**
   * @param a asdasd
   * @param b asdasd
   * @param c asdasd
   * @param d asdasd
   */
  @Converts
  public void voidAndTooManyParameters(int a, int b, int c, int d) {
    // empty block
  }
}