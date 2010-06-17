package transmuter.converter.exception;

import java.lang.reflect.Method;

public class InaccessibleMethodException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private Method method;

  public InaccessibleMethodException(Method method) {
    super(method + " cannot be externally accessed");
    
    this.method = method;
  }

  public Method getMethod() {
    return method;
  }
}
