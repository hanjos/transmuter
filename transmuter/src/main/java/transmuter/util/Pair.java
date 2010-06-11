package transmuter.util;

import static transmuter.util.ObjectUtils.areEqual;
import static transmuter.util.ObjectUtils.hashCodeOf;
import static transmuter.util.ObjectUtils.nonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.gentyref.CaptureType;
import com.googlecode.gentyref.GenericTypeReflector;

import transmuter.exception.InvalidParameterTypeException;
import transmuter.exception.InvalidReturnTypeException;
import transmuter.exception.PairInstantiationException;
import transmuter.exception.WrongParameterCountException;
import transmuter.type.TypeToken;
import transmuter.type.TypeToken.ValueType;

public class Pair {
  private TypeToken<?> fromType;
  private TypeToken<?> toType;

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
    
    return fromMethod(method, method.getDeclaringClass());
  }
  
  public static Pair fromMethod(Method method, Type ownerType) throws PairInstantiationException {
    if(method == null)
      throw new PairInstantiationException(new IllegalArgumentException("method"));
    
    if(ownerType == null)
      throw new PairInstantiationException(new IllegalArgumentException("ownerType"));
    
    if(ownerType instanceof Class<?>)
      ownerType = GenericTypeReflector.addWildcardParameters((Class<?>) ownerType);
    
    List<Exception> exceptions = new ArrayList<Exception>();
    
    TypeToken<?> parameterToken = null;
    TypeToken<?> returnToken = null;
    
    // getting the parameter type
    final Type[] parameterTypes = GenericTypeReflector.getExactParameterTypes(method, ownerType);
    final int parameterCount = parameterTypes.length;
    if(parameterCount == 0 || parameterCount > 1) {
      exceptions.add(new WrongParameterCountException(method, 1));
    } else {
      Type parameterType = parameterTypes[0];
      
      if(parameterType == null // XXX means it's a generic method (for now) 
      || parameterType instanceof CaptureType) { 
        exceptions.add(new InvalidParameterTypeException(method));
      } else {
        try {
          parameterToken = TypeToken.get(parameterType);
        } catch(Exception e) {
          exceptions.add(e);
        }
      }
    }
    
    // getting the return type
    final Type returnType = GenericTypeReflector.getExactReturnType(method, ownerType);
    if(returnType == null // XXX means it's a generic method (for now) 
    || returnType instanceof CaptureType
    || TypeToken.ValueType.VOID.matches(returnType)) {
      exceptions.add(new InvalidReturnTypeException(method));
    } else {
      try {
        returnToken = TypeToken.get(returnType);
      } catch(Exception e) {
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
    
    return getFromType().isAssignableFrom(pair.getFromType())
        && getToType().isAssignableFrom(pair.getToType());
  }
  
  // utility methods
  @Override
  public String toString() {
    return getFromType() + " -> " + getToType();
  }
    
  @Override
  public int hashCode() {
    final int prime = 31;
    
    // XXX we must use the ValueType's hashCode for this calculation,
    // since we are 'equaling' Pairs with matching primitive/wrapper types. 
    // HashMap's search algorithm fails otherwise
    
    // null if fromType is not a value type
    final ValueType<?> fromTypeVT = ValueType.valueOf(getFromType());
    
    // null if toType is not a value type
    final ValueType<?> toTypeVT = ValueType.valueOf(getToType()); 
    
    final int fromHC = hashCodeOf(fromTypeVT != null ? fromTypeVT : getFromType());
    final int toHC = hashCodeOf(toTypeVT != null ? toTypeVT : getToType());
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
    
    return areEquivalent(getFromType(), other.getFromType())
        && areEquivalent(getToType(), other.getToType());
  }

  protected static boolean areEquivalent(TypeToken<?> from, TypeToken<?> otherFrom) {
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
