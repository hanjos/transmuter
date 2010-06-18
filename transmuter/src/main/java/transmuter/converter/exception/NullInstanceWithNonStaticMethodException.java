package transmuter.converter.exception;

import java.lang.reflect.Method;

/**
 * Thrown when there is an attempt to {@link Binding bind} a non-static method to a null instance.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class NullInstanceWithNonStaticMethodException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private static String buildMessage(Method method) {
    return "Cannot bind a null instance to non-static method " + method;
  }

  private Method method;
  
  public NullInstanceWithNonStaticMethodException(Method method) {
    super(buildMessage(method));
    
    this.method = method;
  }

  /**
   * @return the faulty method.
   */
  public Method getMethod() {
    return method;
  }
}
