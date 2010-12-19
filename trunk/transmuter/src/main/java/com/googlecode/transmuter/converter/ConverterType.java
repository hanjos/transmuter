package com.googlecode.transmuter.converter;

import static com.googlecode.gentyref.GenericTypeReflector.addWildcardParameters;
import static com.googlecode.gentyref.GenericTypeReflector.getExactParameterTypes;
import static com.googlecode.gentyref.GenericTypeReflector.getExactReturnType;
import static com.googlecode.transmuter.util.ObjectUtils.hashCodeOf;
import static com.googlecode.transmuter.util.ObjectUtils.nonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.gentyref.CaptureType;
import com.googlecode.transmuter.converter.exception.InvalidParameterTypeException;
import com.googlecode.transmuter.converter.exception.InvalidReturnTypeException;
import com.googlecode.transmuter.converter.exception.MethodOwnerTypeIncompatibilityException;
import com.googlecode.transmuter.converter.exception.WrongParameterCountException;
import com.googlecode.transmuter.type.TypeToken;
import com.googlecode.transmuter.type.TypeToken.ValueType;
import com.googlecode.transmuter.util.ReflectionUtils;
import com.googlecode.transmuter.util.exception.ObjectInstantiationException;

/**
 * Represents a converter's "type": the input type (called {@code fromType}) paired with its output type 
 * (called {@code toType}). Converter types are immutable.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class ConverterType {
  private TypeToken<?> fromType;
  private TypeToken<?> toType;

  /**
   * Creates a new converter type. Throws an exception if one of the arguments is null or 
   * a void type: {@link Void#TYPE void.class} or {@link Void Void.class}. 
   * 
   * @param fromType the input type.
   * @param toType the output type.
   * @throws IllegalArgumentException if one of the arguments is null or void.
   */
  public ConverterType(Type fromType, Type toType) {
    this(TypeToken.get(fromType), TypeToken.get(toType));
  }
  
  /**
   * Creates a new converter type. Throws an exception if one of the arguments is null or 
   * a void type: {@link Void#TYPE void.class} or {@link Void Void.class}. 
   * 
   * @param fromType the input type.
   * @param toType the output type.
   * @throws IllegalArgumentException if one of the arguments is null or void.
   */
  public ConverterType(TypeToken<?> fromType, TypeToken<?> toType) {
    this.fromType = nonNullOrVoid(fromType, "fromType");
    this.toType = nonNullOrVoid(toType, "toType");
  }

  /* (non-javadoc)
   * Throws IllegalArgumentException if object is null or void.class. 
   */
  private static TypeToken<?> nonNullOrVoid(TypeToken<?> object, String varName) {
    if(object == null || TypeToken.ValueType.VOID.matches(object))
      throw new IllegalArgumentException(varName + ": " + object);
    
    return object;
  } 
  
  // factory methods
  /**
   * Creates a new {@code ConverterType} extracting the information from a method object. Calling this method is 
   * equivalent to calling {@link #from(Method, Type)} with {@code method} and {@code method}'s declaring class.
   * 
   * @param method a method object. Cannot be null.
   * @return a new {@code ConverterType} instance.
   * @throws ObjectInstantiationException if a converter type could not be made.
   * @see #from(Method, Type)
   */
  public static ConverterType from(Method method) throws ObjectInstantiationException {
    try {
      nonNull(method, "method");
      
      return from(method, method.getDeclaringClass());
    } catch (ObjectInstantiationException e) {
      throw e;
    } catch (Exception e) {
      throw new ObjectInstantiationException(ConverterType.class, e);
    }
  }
  
  /**
   * Creates a new {@code ConverterType} extracting the information from a given instance and a method object. 
   * Calling this method is equivalent to calling {@link #from(Method, Type)} with {@code method} and its 
   * {@link ReflectionUtils#getOwnerType(Object, Method) owner type}.
   * 
   * @param instance an object. May be null. 
   * @param method a method object. Cannot be null.
   * @return a new {@code ConverterType} instance.
   * @throws ObjectInstantiationException if a converter type could not be made.
   * @see #from(Method, Type)
   */
  public static ConverterType from(Object instance, Method method) throws ObjectInstantiationException {
    try {
      nonNull(method, "method");
      
      return from(method, ReflectionUtils.getOwnerType(instance, method));
    } catch (ObjectInstantiationException e) {
      throw e;
    } catch (Exception e) {
      throw new ObjectInstantiationException(ConverterType.class, e);
    }
  }
  
  /**
   * Creates a new converter type extracting the information from a method object and it's owner type. The owner type 
   * is used to resolve generic types referenced or inherited by the method.
   * <p>
   * Several checks are made to see if the given method is a valid converter method, with each error associated to an 
   * exception. All the exceptions, if any, are bundled up in a {@link ObjectInstantiationException} for 
   * throwing. The checks made are:
   * 
   * <ul>
   * <li>{@code method} cannot be null. This error forces a 
   * {@code ConverterTypeInstantiationException} containing a single 
   * {@link IllegalArgumentException}.</li>
   * <li>{@code ownerType} cannot be null. This error forces a 
   * {@code ConverterTypeInstantiationException} containing a single 
   * {@link IllegalArgumentException}.</li>
   * <li>{@code method} must be invokable on {@code ownerType}. This error 
   * forces a {@code ConverterTypeInstantiationException} containing a single 
   * {@link MethodOwnerTypeIncompatibilityException}.</li>
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
   * @param ownerType the specific instance class to which the given method belongs.
   * @return a new {@link ConverterType} instance.
   * @throws ObjectInstantiationException if there are errors while extracting the information.
   */
  public static ConverterType from(Method method, Type ownerType) throws ObjectInstantiationException {
    try {
      nonNull(method, "method");
      nonNull(ownerType, "ownerType");
      
      if(ownerType instanceof Class<?>)
        // fills any generic parameters found with wildcards, which helps when resolving method's 
        // parameter and return types
        ownerType = addWildcardParameters((Class<?>) ownerType);
      
      if(! ReflectionUtils.isCompatible(method, ownerType))
        throw new MethodOwnerTypeIncompatibilityException(method, ownerType);
      
      List<Exception> exceptions = new ArrayList<Exception>();
      
      TypeToken<?> parameterToken = extractParameterToken(method, ownerType, exceptions);
      TypeToken<?> returnToken = extractReturnToken(method, ownerType, exceptions);
      
      if(exceptions.size() > 0)
        throw new ObjectInstantiationException(ConverterType.class, exceptions);
      
      return new ConverterType(parameterToken, returnToken);
    } catch (ObjectInstantiationException e) {
      throw e;
    } catch (Exception e) {
      throw new ObjectInstantiationException(ConverterType.class, e);
    }
  }

  private static TypeToken<?> extractParameterToken(Method method, Type ownerType, List<Exception> exceptions) {
    try {
      Type[] parameterTypes = getExactParameterTypes(method, ownerType);
      if(parameterTypes.length != 1)
        throw new WrongParameterCountException(method, 1);
      
      Type parameterType = parameterTypes[0];
      if(parameterType == null // XXX means it's a generic method; this may change in later versions of Gentyref 
      || parameterType instanceof CaptureType)
        throw new InvalidParameterTypeException(method);
      
      return TypeToken.get(parameterType);
    } catch (Exception e) {
      exceptions.add(e);
    }
  
    return null;
  }
  
  private static TypeToken<?> extractReturnToken(Method method, Type ownerType, List<Exception> exceptions) {
    try {
      Type returnType = getExactReturnType(method, ownerType);
      
      if(returnType == null // XXX means it's a generic method; this may change in later versions of Gentyref
      || returnType instanceof CaptureType
      || TypeToken.ValueType.VOID.matches(returnType))
        throw new InvalidReturnTypeException(method);
      
      return TypeToken.get(returnType);
    } catch (Exception e) {
      exceptions.add(e);
    }
    
    return null;
  }

  /**
   * Creates a new {@code ConverterType} instance using the information from the given binding. Calling this is equivalent 
   * to calling {@link #from(Object, Method)} with the binding's {@link Binding#getInstance() instance} and 
   * {@link Binding#getMethod() method}.
   * 
   * @param binding a binding.
   * @return a {@code ConverterType} instance constructed from {@code binding}.
   * @throws ObjectInstantiationException if binding is null or cannot be used to create a converter type.
   * @see #from(Method, Type)
   * @see Binding#getInstanceClass()
   * @see Binding#getMethod()
   */
  public static ConverterType from(Binding binding) throws ObjectInstantiationException {
    try {
      nonNull(binding, "binding");
      
      return from(binding.getInstance(), binding.getMethod());
    } catch (ObjectInstantiationException e) {
      throw e;
    } catch (Exception e) {
      throw new ObjectInstantiationException(ConverterType.class, e);
    }
  }
  
  // operations
  /**
   * Checks if the types used in the given converter type are also compatible with the types in this instance.
   * 
   * @param converterType a converter type.
   * @return if the types used in the given converter type are also compatible with the types in this instance, 
   * or {@code false} if the given converter type is null.
   */
  public boolean isAssignableFrom(ConverterType converterType) {
    if(converterType == null)
      return false;
    
    // ??? is a raw converter type assignable from a generic converter type?
    return getFromType().isAssignableFrom(converterType.getFromType())
        && getToType().isAssignableFrom(converterType.getToType());
  }
  
  // utility methods
  /**
   * Returns a string representation of this object.
   * 
   * @return a string representation of this object.
   */
  @Override
  public String toString() {
    return getFromType() + " -> " + getToType();
  }
  
  /**
   * Two converter types are equal if they are {@link #isAssignableFrom(ConverterType) assignable from} each other.
   * <p>
   * This is the same as checking the equality of its types, with one exception: converter types containing primitive 
   * types are considered equal to converter types which differ only by holding the correspondent wrapper types.
   * 
   * Examples:
   * 
   * <pre>
   * new ConverterType(String, boolean).equals(new ConverterType(String, Boolean))
   * new ConverterType(Byte, Object)   .equals(new ConverterType(byte, Object))
   * new ConverterType(int, Character) .equals(new ConverterType(Integer, char))
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
    
    ConverterType other = (ConverterType) obj;
    
    return this.isAssignableFrom(other)
        && other.isAssignableFrom(this);
  }

  /**
   * Given {@code ConverterType}'s {@link #equals(Object) equality} definition, converter types 
   * with primitive types must have the same hash code as equivalent converter types
   * with corresponding wrapper types.
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    
    // XXX we must use the ValueType's hashCode for this calculation,
    // since we are 'equaling' ConverterTypes with matching primitive/wrapper types. 
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
   * Returns the input type accepted by this instance.
   * 
   * @return the input type accepted by this instance.
   */
  public TypeToken<?> getFromType() {
    return fromType;
  }

  /**
   * Returns the output type accepted by this instance.
   * 
   * @return the output type accepted by this instance.
   */
  public TypeToken<?> getToType() {
    return toType;
  }
}
