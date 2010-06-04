package transmuter.util;

import static transmuter.util.ObjectUtils.areEqual;
import static transmuter.util.ObjectUtils.hashCodeOf;
import static transmuter.util.ObjectUtils.nonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.gentyref.GenericTypeReflector;

import transmuter.exception.InvalidParameterTypeException;
import transmuter.exception.InvalidReturnTypeException;
import transmuter.exception.PairInstantiationException;
import transmuter.exception.WrongParameterCountException;
import transmuter.type.TypeToken;
import transmuter.type.TypeToken.ValueType;

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
  public static Pair fromMethod(Method method) throws PairInstantiationException {
    if(method == null)
      throw new PairInstantiationException(new IllegalArgumentException("method"));
        
    List<Exception> exceptions = new ArrayList<Exception>();
    
    TypeToken<?> parameterToken = null;
    TypeToken<?> returnToken = null;
    
    // getting the parameter type
    final Type[] parameterTypes = GenericTypeReflector.getExactParameterTypes(method, method.getDeclaringClass());
    final int parameterCount = parameterTypes.length;
    if(parameterCount == 0 || parameterCount > 1) {
      exceptions.add(new WrongParameterCountException(method, 1));
    } else {
      // FIXME: Gentyref couldn't deal with it, so it's a generic method
      if(parameterTypes[0] == null) { 
        exceptions.add(new InvalidParameterTypeException(method));
      } else {
        try {
          parameterToken = TypeToken.get(parameterTypes[0]);
        } catch(Exception e) { // type token building may throw exceptions
          exceptions.add(e);
        }
      }
    }
    
    // getting the return type
    
    // FIXME: Gentyref couldn't deal with it, so it's a generic method
    final Type returnType = GenericTypeReflector.getExactReturnType(method, method.getDeclaringClass());
    if(returnType == null || TypeToken.ValueType.VOID.matches(returnType)) {
      exceptions.add(new InvalidReturnTypeException(method));
    } else {
      try {
        returnToken = TypeToken.get(returnType);
      } catch(Exception e) { // type token building may throw exceptions
        exceptions.add(e);
      }
    }
    
    if(exceptions.size() > 0)
      throw new PairInstantiationException(exceptions);
    
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
    
    // XXX we must use the ValueType's hashCode for this calculation,
    // since we are 'equaling' Pairs with matching primitive/wrapper types. 
    // HashMap's searching algorithm fails otherwise
    
    // null if fromType is not a value type
    final ValueType fromTypeVT = ValueType.valueOf(fromType);
    
    // null if toType is not a value type
    final ValueType toTypeVT = ValueType.valueOf(toType); 
    
    final int fromHC = hashCodeOf(fromTypeVT != null ? fromTypeVT : fromType);
    final int toHC = hashCodeOf(toTypeVT != null ? toTypeVT : toType);
    return prime * (prime + fromHC) + toHC;
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
    
    return areEquivalent(fromType, other.fromType)
        && areEquivalent(toType, other.toType);
  }

  // TODO put this somewhere else?
  private static boolean areEquivalent(TypeToken<?> from, TypeToken<?> otherFrom) {
    return areEqual(from, otherFrom)
        || (   (ValueType.valueOf(from) != null) 
            && (ValueType.valueOf(from) == ValueType.valueOf(otherFrom)));
  }

  // properties
  public TypeToken<?> getFromType() {
    return fromType;
  }

  public TypeToken<?> getToType() {
    return toType;
  }
}
