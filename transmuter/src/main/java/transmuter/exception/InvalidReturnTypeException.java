package transmuter.exception;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import transmuter.util.ReflectionUtils;

public class InvalidReturnTypeException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private static Type getReturnTypeOf(Method method) {
    if(method == null)
      return null;
    
    return method.getGenericReturnType();
  }
  
  private Method method;
  private Type type;

  public InvalidReturnTypeException(Method method) {
    super(getReturnTypeOf(method) + " is an invalid return type in " + ReflectionUtils.simpleMethodToString(method));
    
    this.method = method;
    this.type = getReturnTypeOf(method);
  }

  public Method getMethod() {
    return method;
  }

  public Type getType() {
    return type;
  }
}
