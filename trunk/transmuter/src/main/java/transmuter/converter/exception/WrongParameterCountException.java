package transmuter.converter.exception;

import java.lang.reflect.Method;

import transmuter.util.ReflectionUtils;

/**
 * Thrown when the given method has an invalid number of parameters. 
 * 
 * @author Humberto S. N. dos Anjos
 */
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
  
  /**
   * @param method the faulty method.
   * @param expected the expected number of parameters.
   */
  public WrongParameterCountException(Method method, int expected) {
    super(buildMessage(expected, method));
    
    this.method = method;
    this.actual = extractActual(method);
    this.expected = expected;
  }

  /**
   * @return the method.  
   */
  public Method getMethod() {
    return method;
  }

  /**
   * @return the expected number of parameters.
   */
  public int getExpected() {
    return expected;
  }
  
  /**
   * @return the actual number of parameters, or {@code 0} if {@code method} is {@code null}.
   */
  public int getActual() {
    return actual;
  }
}