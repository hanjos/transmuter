package transmuter.exception;

import java.lang.reflect.Method;

import transmuter.type.TypeToken;

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
    this(method, getReturnTypeOf(method) + " is not a valid return type!");
  }
  
  public InvalidReturnTypeException(Method method, String message) {
    super(message);
    
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
