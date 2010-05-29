package transmuter.exception;

import java.lang.reflect.Method;

import transmuter.type.TypeToken;
import transmuter.util.ReflectionUtils;

public class InvalidReturnTypeException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  private static TypeToken<?> getReturnTypeOf(Method method) {
    if(method == null)
      return null;
    
    return TypeToken.get(method.getGenericReturnType());
  }
  
  private Method method;
  private TypeToken<?> returnType;

  public InvalidReturnTypeException(Method method) {
    super(getReturnTypeOf(method) + " is not a valid return type for " + ReflectionUtils.simpleMethodToString(method));
    
    this.method = method;
    this.returnType = getReturnTypeOf(method);
  }

  public Method getMethod() {
    return method;
  }

  public TypeToken<?> getReturnType() {
    return returnType;
  }
}
