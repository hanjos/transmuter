package transmuter;

import static com.googlecode.gentyref.GenericTypeReflector.addWildcardParameters;
import static com.googlecode.gentyref.GenericTypeReflector.capture;
import static com.googlecode.gentyref.GenericTypeReflector.getExactParameterTypes;
import static com.googlecode.gentyref.GenericTypeReflector.getExactReturnType;
import static com.googlecode.gentyref.GenericTypeReflector.getExactSuperType;
import static transmuter.util.ObjectUtils.hashCodeOf;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import transmuter.exception.InvalidParameterTypeException;
import transmuter.exception.InvalidReturnTypeException;
import transmuter.exception.PairInstantiationException;
import transmuter.exception.WrongParameterCountException;
import transmuter.type.TypeToken;
import transmuter.type.TypeToken.ValueType;
import transmuter.util.exception.MethodOwnerTypeIncompatibilityException;

import com.googlecode.gentyref.CaptureType;

/**
 * Represents a converter's "type": the input type paired with its output type.
 * Pairs are immutable.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class Pair {
  private TypeToken<?> fromType;
  private TypeToken<?> toType;

  /**
   * Creates a new pair. Throws an exception if one of the arguments is null or 
   * a void type: {@link Void#TYPE void.class} or {@link Void Void.class}. 
   * 
   * @param fromType the input type.
   * @param toType the output type.
   * @throws IllegalArgumentException if one of the arguments is null or void.
   */
  public Pair(Type fromType, Type toType) {
    this(TypeToken.get(fromType), TypeToken.get(toType));
  }
  
  /**
   * Creates a new pair. Throws an exception if one of the arguments is null or 
   * a void type: {@link Void#TYPE void.class} or {@link Void Void.class}. 
   * 
   * @param fromType the input type.
   * @param toType the output type.
   * @throws IllegalArgumentException if one of the arguments is null or void.
   */
  public Pair(TypeToken<?> fromType, TypeToken<?> toType) {
    this.fromType = nonNullOrVoid(fromType, "fromType");
    this.toType = nonNullOrVoid(toType, "toType");
  }

  private static TypeToken<?> nonNullOrVoid(TypeToken<?> object, String varName) {
    if(object == null || TypeToken.ValueType.VOID.matches(object))
      throw new IllegalArgumentException(varName + ": " + object);
    
    return object;
  } 
  
  // factory methods
  /**
   * Creates a new {@code Pair} extracting the information from a method 
   * object. Calling this method is equivalent to calling 
   * {@link #fromMethod(Method, Type)} with {@code method} and {@code method}'s
   * declaring class.
   * 
   * @param method a method object.
   * @return a new {@code Pair} instance.
   * @throws PairInstantiationException if a pair could not be made.
   * @see #fromMethod(Method, Type)
   */
  public static Pair fromMethod(Method method) throws PairInstantiationException {
    if(method == null)
      throw new PairInstantiationException(new IllegalArgumentException("method"));
    
    return fromMethod(method, method.getDeclaringClass());
  }
  
  /**
   * Creates a new pair extracting the information from a method object and 
   * it's owner type. The owner type is used to resolve generic types referenced or 
   * inherited by the method.
   * <p>
   * Several checks are made to see if the given method is a valid converter
   * method, with each error associated to an exception. All the exceptions, 
   * if any, are bundled up in a {@link PairInstantiationException} for 
   * throwing. The checks made are:
   * 
   * <ul>
   * <li>{@code method} cannot be null. This error forces a 
   * {@code PairInstantiationException} containing a single 
   * {@link IllegalArgumentException}</li>
   * <li>{@code ownerType} cannot be null. This error forces a 
   * {@code PairInstantiationException} containing a single 
   * {@link IllegalArgumentException}</li>
   * <li>{@code method} must be invokable on {@code ownerType}. This error 
   * forces a {@code PairInstantiationException} containing a single 
   * {@link MethodOwnerTypeIncompatibilityException}).</li>
   * <li>{@code method} must have one and only one parameter 
   * (signals a {@link WrongParameterCountException}).</li>
   * <li>{@code method}'s parameter must be a non-generic type 
   * (signals a {@link InvalidParameterTypeException}).</li>
   * <li>{@code method}'s return type cannot be {@code void} 
   * (signals a {@link InvalidReturnTypeException}).</li>
   * <li>{@code method}'s return type must also be non-generic 
   * (signals a {@link InvalidReturnTypeException}).</li>
   * </ul>
   * 
   * @param method a method object.
   * @param ownerType the specific instance class to which {@code method} belongs.
   * @return a new {@code Pair} instance.
   * @throws PairInstantiationException if there are errors while extracting 
   * the information.
   */
  public static Pair fromMethod(Method method, Type ownerType) throws PairInstantiationException {
    if(method == null)
      throw new PairInstantiationException(new IllegalArgumentException("method"));
    
    if(ownerType == null)
      throw new PairInstantiationException(new IllegalArgumentException("ownerType"));
    
    if(ownerType instanceof Class<?>)
      ownerType = addWildcardParameters((Class<?>) ownerType);
    
    if(! isCompatible(method, ownerType))
      throw new PairInstantiationException(new MethodOwnerTypeIncompatibilityException(method, ownerType));
    
    List<Exception> exceptions = new ArrayList<Exception>();
    
    TypeToken<?> parameterToken = extractParameterToken(method, ownerType, exceptions);
    TypeToken<?> returnToken = extractReturnToken(method, ownerType, exceptions);
    
    if(exceptions.size() > 0)
      throw new PairInstantiationException(exceptions);
    
    return new Pair(parameterToken, returnToken);
  }

  /**
   * @return {@code true} if {@code ownerType} is a subtype of {@code method}'s declaring class.
   */
  protected static boolean isCompatible(Method method, Type ownerType) {
    return getExactSuperType(capture(ownerType), method.getDeclaringClass()) != null;
  }

  private static TypeToken<?> extractParameterToken(Method method, Type ownerType, List<Exception> exceptions) {
    Type[] parameterTypes = null; 
    
    try {
      parameterTypes = getExactParameterTypes(method, ownerType);
    } catch(Exception e) {
      exceptions.add(e);
      return null;
    }
    
    if(parameterTypes.length == 0 || parameterTypes.length > 1) {
      exceptions.add(new WrongParameterCountException(method, 1));
      return null;
    }
    
    Type parameterType = parameterTypes[0];
    if(parameterType == null // means it's a generic method 
    || parameterType instanceof CaptureType) { 
      exceptions.add(new InvalidParameterTypeException(method));
      return null;
    }
    
    try {
      return TypeToken.get(parameterType);
    } catch(Exception e) {
      exceptions.add(e);
    }
  
    return null;
  }
  
  private static TypeToken<?> extractReturnToken(Method method, Type ownerType, List<Exception> exceptions) {
    Type returnType = null; 
    try {
      returnType = getExactReturnType(method, ownerType);
    } catch(Exception e) {
      exceptions.add(e);
      return null;
    }
    
    if(returnType == null // means it's a generic method 
    || returnType instanceof CaptureType
    || TypeToken.ValueType.VOID.matches(returnType)) {
      exceptions.add(new InvalidReturnTypeException(method));
      return null;
    }
    
    try {
      return TypeToken.get(returnType);
    } catch(Exception e) {
      exceptions.add(e);
    }
    
    return null;
  }

  /**
   * Creates a new {@code Pair} instance using the information from the given binding. Calling this is equivalent 
   * to calling {@link #fromMethod(Method, Type)} with the binding's {@link Binding#getMethod() method} and
   * {@link Binding#getInstanceClass() instance class}.
   * 
   * @param binding a binding.
   * @return a {@code Pair} instance constructed from {@code binding}.
   * @throws PairInstantiationException if binding is null or cannot be used to create a pair.
   */
  public static Pair fromBinding(Binding binding) throws PairInstantiationException {
    if(binding == null)
      throw new PairInstantiationException(new IllegalArgumentException("binding"));
    
    return Pair.fromMethod(binding.getMethod(), binding.getInstanceClass());
  }
  
  // operations
  /**
   * Checks if the types used in the given pair are also compatible with the types in this pair.
   * 
   * @param pair a pair.
   * @return if the types used in {@code pair} are also compatible with the types in this pair, 
   * or {@code false} if {@code pair} is null.
   */
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
  
  /**
   * Two pairs are equal if they are {@link #isAssignableFrom(Pair) assignable from} each other.
   * <p>
   * This is the same as checking the equality of its types, with one exception: pairs containing primitive types 
   * are considered equal to pairs which differ only by holding the correspondent wrapper types.
   * 
   * Examples:
   * 
   * <pre>
   * new Pair(String, boolean).equals(new Pair(String, Boolean))
   * new Pair(Byte, Object)   .equals(new Pair(byte, Object))
   * new Pair(int, Character) .equals(new Pair(Integer, char))
   * </pre>
   */
  @Override
  public boolean equals(Object obj) {
    if(this == obj)
      return true;
    
    if(obj == null)
      return false;
    
    if(getClass() != obj.getClass())
      return false;
    
    Pair other = (Pair) obj;
    
    return this.isAssignableFrom(other)
        && other.isAssignableFrom(this);
  }

  /**
   * Given {@code Pair}'s {@link #equals(Object) equality} definition, pairs 
   * with primitive types must have the same hash code as equivalent pairs
   * with corresponding wrapper types.
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    
    // we must use the ValueType's hashCode for this calculation,
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

  // properties
  /**
   * @return the input type accepted by this pair.
   */
  public TypeToken<?> getFromType() {
    return fromType;
  }

  /**
   * @return the output type accepted by this pair.
   */
  public TypeToken<?> getToType() {
    return toType;
  }
}
