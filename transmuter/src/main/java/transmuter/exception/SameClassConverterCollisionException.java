package transmuter.exception;

import java.lang.reflect.Type;

import transmuter.type.TypeToken;

public class SameClassConverterCollisionException extends ConverterCollisionException {
  private static final long serialVersionUID = 1L;

  private static String buildMessage(TypeToken<?> declaringType, TypeToken<?> fromType, TypeToken<?> toType) {
    return "More than one converter for " + fromType + " to " + toType + " found in " + declaringType;
  }
  
  private TypeToken<?> declaringType;

  public SameClassConverterCollisionException(Type declaringType, Type fromType, Type toType) {
    this(TypeToken.get(declaringType), TypeToken.get(fromType), TypeToken.get(toType));
  }

  public SameClassConverterCollisionException(Type declaringType, Type fromType, Type toType, String message) {
    this(TypeToken.get(declaringType), TypeToken.get(fromType), TypeToken.get(toType), message);
  }

  public SameClassConverterCollisionException(TypeToken<?> declaringType, TypeToken<?> fromType, TypeToken<?> toType) {
    this(declaringType, fromType, toType, buildMessage(declaringType, fromType, toType));
  }

  public SameClassConverterCollisionException(TypeToken<?> declaringType, TypeToken<?> fromType, TypeToken<?> toType,
      String message) {
    super(fromType, toType, message);
    
    this.declaringType = declaringType;
  }

  public Object getDeclaringType() {
    return declaringType;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((declaringType == null) ? 0 : declaringType.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if(! super.equals(obj))
      return false;
    
    if(getClass() != obj.getClass())
      return false;
    
    SameClassConverterCollisionException other = (SameClassConverterCollisionException) obj;
    if(getFromType() == null) {
      if(other.getFromType() != null)
        return false;
    } else if(! getFromType().equals(other.getFromType()))
      return false;
    if(getToType() == null) {
      if(other.getToType() != null)
        return false;
    } else if(! getToType().equals(other.getToType()))
      return false;
    
    if(declaringType == null) {
      if(other.declaringType != null)
        return false;
    } else if(! declaringType.equals(other.declaringType))
      return false;
    return true;
  }
}
