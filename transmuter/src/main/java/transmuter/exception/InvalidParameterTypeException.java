package transmuter.exception;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import transmuter.util.ReflectionUtils;

public class InvalidParameterTypeException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private static Type getParameterTypeOf(Method method) {
    if(method == null || method.getParameterTypes().length == 0)
      return null;
    
    return method.getGenericParameterTypes()[0];
  }
  
  private Method method;
  private Type type;
  
  public InvalidParameterTypeException(Method method) {
    super(getParameterTypeOf(method) + " is an invalid parameter type in " + ReflectionUtils.simpleMethodToString(method));
    
    this.method = method;
    this.type = getParameterTypeOf(method);
  }

  public Method getMethod() {
    return method;
  }

  public Type getType() {
    return type;
  }
}
