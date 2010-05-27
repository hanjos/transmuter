package transmuter.util;

import static transmuter.util.ObjectUtils.areEqual;
import static transmuter.util.ObjectUtils.hashCodeOf;
import static transmuter.util.ObjectUtils.nonNull;

import java.lang.reflect.Type;

import transmuter.type.TypeToken;

public class Pair {
  private final TypeToken<?> fromType;
  private final TypeToken<?> toType;

  public Pair(Type fromType, Type toType) {
    this(TypeToken.get(fromType), TypeToken.get(toType));
  }
  
  public Pair(TypeToken<?> fromType, TypeToken<?> toType) {
    this.fromType = nonNull(fromType, "fromType");
    this.toType = nonNull(toType, "toType");
  }

  // operations
  public boolean isAssignableFrom(Pair pair) {
    if(pair == null)
      return false;
    
    return fromType.isAssignableFrom(pair.fromType)
        && toType.isAssignableFrom(pair.toType);
  }
  
  // utility methods
  @Override
  public String toString() {
    return fromType + " -> " + toType;
  }
    
  @Override
  public int hashCode() {
    final int prime = 31;
    return prime * (prime + hashCodeOf(fromType)) + hashCodeOf(toType);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj)
      return true;
    
    if(obj == null)
      return false;
    
    if(getClass() != obj.getClass())
      return false;
    
    Pair other = (Pair) obj;
    
    return areEqual(fromType, other.fromType)
        && areEqual(toType, other.toType);
  }

  // properties
  public TypeToken<?> getFromType() {
    return fromType;
  }

  public TypeToken<?> getToType() {
    return toType;
  }
}
