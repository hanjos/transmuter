package transmuter.converter.exception;

import java.lang.reflect.Method;

import transmuter.util.ReflectionUtils;

public class WrongParameterCountException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private static int extractActual(Method method) {
    return (method != null) ? method.getParameterTypes().length : 0;
  }
  
  private static String buildMessage(int expected, Method method) {
    return "expected " + expected + ", got " + extractActual(method) 
         + " from " + ReflectionUtils.simpleMethodToString(method);
  }

  private Method method;
  private int actual;
  private int expected;
  
  public WrongParameterCountException(Method method, int expected) {
    this(method, expected, buildMessage(expected, method));
  }

  public WrongParameterCountException(Method method, int expected, String message) {
    super(message);
    
    this.method = method;
    this.actual = extractActual(method);
    this.expected = expected;
  }

  public Method getMethod() {
    return method;
  }

  public int getExpected() {
    return expected;
  }
  
  public int getActual() {
    return actual;
  }
}
