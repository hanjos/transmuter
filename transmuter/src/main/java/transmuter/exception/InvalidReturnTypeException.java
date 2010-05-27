package transmuter.exception;

import java.lang.reflect.Type;

import transmuter.type.TypeToken;

public class InvalidReturnTypeException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private TypeToken<?> returnType;

  public InvalidReturnTypeException(Type returnType) {
    this(TypeToken.get(returnType));
  }
  
  public InvalidReturnTypeException(TypeToken<?> returnType) {
    super(returnType + " is not a valid return type!");
    
    this.returnType = returnType;
  }

  public TypeToken<?> getReturnType() {
    return returnType;
  }
}
