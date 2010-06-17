package transmuter.converter.exception;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class MethodOwnerTypeIncompatibilityException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private static String buildMessage(Method method, Type ownerType) {
    return "The method " + method + " is not compatible with " + ownerType;
  }
  
  private Method method;
  private Type ownerType;

  public MethodOwnerTypeIncompatibilityException(Method method, Type ownerType) {
    super(buildMessage(method, ownerType));
    
    this.method = method;
    this.ownerType = ownerType;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  public Method getMethod() {
    return method;
  }

  public Type getOwnerType() {
    return ownerType;
  }
}
