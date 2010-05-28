package transmuter.util.exception;

import java.lang.reflect.Method;

public class MethodInstanceIncompatibilityException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private static String buildMessage(Object instance, Method method) {
    return "The method " + method + " cannot be invoked on " + instance;
  }

  private Object instance;
  private Method method;
  
  public MethodInstanceIncompatibilityException(Object instance, Method method) {
    super(buildMessage(instance, method));
    
    this.instance = instance;
    this.method = method;
  }

  public Object getInstance() {
    return instance;
  }

  public Method getMethod() {
    return method;
  }
}
