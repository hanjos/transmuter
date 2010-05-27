package transmuter.exception;

import java.lang.reflect.Type;

import transmuter.type.TypeToken;

public class ConverterCollisionException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private TypeToken<?> fromType;
  private TypeToken<?> toType;
  
  private static String buildMessage(TypeToken<?> fromType, TypeToken<?> toType) {
    return "More than one converter for " + fromType + " to " + toType;
  }
  
  public ConverterCollisionException(Type fromType, Type toType) {
    this(TypeToken.get(fromType), TypeToken.get(toType));
  }
  
  public ConverterCollisionException(TypeToken<?> fromType, TypeToken<?> toType) {
    this(fromType, toType, buildMessage(fromType, toType));
  }

  public ConverterCollisionException(Type fromType, Type toType, String message) {
    this(TypeToken.get(fromType), TypeToken.get(toType), message);
  }
  
  public ConverterCollisionException(TypeToken<?> fromType, TypeToken<?> toType, String message) {
    super(message);
    
    this.fromType = fromType;
    this.toType = toType;
  }

  public TypeToken<?> getFromType() {
    return fromType;
  }

  public TypeToken<?> getToType() {
    return toType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((fromType == null) ? 0 : fromType.hashCode());
    result = prime * result + ((toType == null) ? 0 : toType.hashCode());
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
    ConverterCollisionException other = (ConverterCollisionException) obj;
    if(fromType == null) {
      if(other.fromType != null)
        return false;
    } else if(! fromType.equals(other.fromType))
      return false;
    if(toType == null) {
      if(other.toType != null)
        return false;
    } else if(! toType.equals(other.toType))
      return false;
    return true;
  }
}
