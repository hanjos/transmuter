package transmuter.util.exception;

import java.lang.reflect.Method;

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

  public Method getMethod() {
    return method;
  }
}
