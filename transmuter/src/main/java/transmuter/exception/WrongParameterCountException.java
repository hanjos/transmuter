package transmuter.exception;

import java.lang.reflect.Method;

public class WrongParameterCountException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private static String buildMessage(int expected, Method method) {
    return "expected " + expected + ", got " 
         + ((method != null) ? method.getParameterTypes().length : 0) + " from " + method;
  }

  private Method method;
  private int expected;
  
  public WrongParameterCountException(Method method, int expected) {
    this(method, expected, buildMessage(expected, method));
  }

  public WrongParameterCountException(Method method, int expected, String message) {
    super(message);
    
    this.method = method;
    this.expected = expected;
  }
  
  public Method getMethod() {
    return method;
  }

  public int getExpected() {
    return expected;
  }
  
  public int getActual() {
    return (method != null) ? method.getParameterTypes().length : 0;
  }
}
