package transmuter.mock;

import transmuter.Converts;

public final class FlawedConverter {
  @Converts
  public boolean intraClassCollision1(int i) {
    return (i == 0) ? false : true;
  }
  
  @Converts
  public boolean intraClassCollision2(int i) {
    return i % 2 == 0;
  }
  
  @Converts
  public void voidAsReturnType(Object whatever) {
    // empty block
  }
  
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
  
  @Converts
  public void voidAndTooManyParameters(int a, int b, int c, int d) {
    // empty block
  }
}