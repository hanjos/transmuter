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
  
  /**
   * @param whatever 
   */
  @Converts
  public void voidAsReturnType(Object whatever) {
    // empty block
  }
  
  /**
   * @param a  
   * @param b 
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
   * @param a  
   * @param b 
   * @param c 
   * @param d 
   */
  @Converts
  public void voidAndTooManyParameters(int a, int b, int c, int d) {
    // empty block
  }
}