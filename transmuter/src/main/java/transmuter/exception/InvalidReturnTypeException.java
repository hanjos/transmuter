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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj)
      return true;
    if(obj == null)
      return false;
    if(getClass() != obj.getClass())
      return false;
    InvalidReturnTypeException other = (InvalidReturnTypeException) obj;
    if(returnType == null) {
      if(other.returnType != null)
        return false;
    } else if(! returnType.equals(other.returnType))
      return false;
    return true;
  }
}
