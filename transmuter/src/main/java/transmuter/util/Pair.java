package transmuter.util;

import static transmuter.util.ObjectUtils.areEqual;
import static transmuter.util.ObjectUtils.hashCodeOf;
import static transmuter.util.ObjectUtils.nonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import transmuter.exception.InvalidReturnTypeException;
import transmuter.exception.PairCreationException;
import transmuter.exception.WrongParameterCountException;
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

  // factory methods
  public static Pair fromMethod(Method method) throws PairCreationException {
    if(method == null)
      throw new PairCreationException(new IllegalArgumentException("method"));
        
    List<Exception> exceptions = new ArrayList<Exception>();
    
    final Type[] parameterTypes = method.getGenericParameterTypes();
    final int parameterCount = parameterTypes.length;
    if(parameterCount == 0 || parameterCount > 1)
      exceptions.add(new WrongParameterCountException(method, 1));
    
    final Type returnType = method.getGenericReturnType();
    if(TypeToken.ValueType.VOID.matches(returnType))
      exceptions.add(new InvalidReturnTypeException(method));
    
    TypeToken<?> parameterToken = null;
    TypeToken<?> returnToken = null;
    
    if(parameterCount == 1) {
      try {
        parameterToken = TypeToken.get(parameterTypes[0]);
      } catch(Exception e) { // type token building may throw exceptions
        exceptions.add(e);
      } 
    }
    
    try {
      returnToken = TypeToken.get(returnType);
    } catch(Exception e) { // type token building may throw exceptions
      exceptions.add(e);
    }
    
    if(exceptions.size() > 0)
      throw new PairCreationException(exceptions);
    
    return new Pair(parameterToken, returnToken);
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
