package com.googlecode.transmuter.converter;

import static com.googlecode.gentyref.GenericTypeReflector.getExactParameterTypes;
import static com.googlecode.gentyref.GenericTypeReflector.getExactReturnType;
import static com.googlecode.transmuter.util.ObjectUtils.areEqual;
import static com.googlecode.transmuter.util.ObjectUtils.hashCodeOf;
import static com.googlecode.transmuter.util.ObjectUtils.nonNull;
import static com.googlecode.transmuter.util.ReflectionUtils.getTypeName;
import static com.googlecode.transmuter.util.ReflectionUtils.getTypeNames;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.transmuter.converter.exception.BindingInstantiationException;
import com.googlecode.transmuter.converter.exception.BindingInvocationException;
import com.googlecode.transmuter.converter.exception.InaccessibleMethodException;
import com.googlecode.transmuter.converter.exception.MethodInstanceIncompatibilityException;
import com.googlecode.transmuter.converter.exception.NullInstanceWithNonStaticMethodException;
import com.googlecode.transmuter.util.StringUtils;


/**
 * Represents an immutable invokable object, made binding a method to an object (which may be {@code null} if the 
 * method is static).
 * <p> 
 * Not all objects, methods or combinations thereof may be bound. If given a {@link Validator validator}, the 
 * constructor will use it to make sure that the given arguments are valid mutually compatible or throw an exception 
 * if they are deemed otherwise. The standard validation (in {@link DefaultValidator}) attempts to ensure that 
 * {@link #invoke(Object...) invoke} calls will not fail due to a method or object invalidity/incompatibility, 
 * i.e. that one cannot build an inherently uninvokable Binding.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class Binding {
  /**
   * Instances of this interface are responsible for ensuring that the given instance and method can be used as a
   * valid binding.
   * 
   * @author Humberto S. N. dos Anjos
   */
  public static interface Validator {
    /**
     * Checks if the given instance and method can be bound. It returns safely if no problem was found, or throws 
     * a {@link BindingInstantiationException} which bundles together the problems found. 
     * 
     * @param instance an object.
     * @param method a method object. 
     * @throws BindingInstantiationException if the given instance and/or method do not constitute a valid binding.
     */
    public void validate(Object instance, Method method) throws BindingInstantiationException;
  }
    
  /**
   * The default validator for {@link Binding}.
   * 
   * @author Humberto S. N. dos Anjos
   */
  public static class DefaultValidator implements Validator {
    /**
     * Several checks are made, each one of them having a corresponding exception on error. If any exceptions are 
     * found, they are collected and bundled into a {@link BindingInstantiationException}. The checks made here are:
     * 
     * <ul>
     * <li>{@code method} may not be {@code null}. This violation throws a {@code BindingInstantiationException} with a 
     * single {@link IllegalArgumentException}.</li>
     * <li>{@code method} must have public visibility (signals an {@link InaccessibleMethodException}).</li>
     * <li>{@code method} must be static if {@code instance} is {@code null} (signals a 
     * {@link NullInstanceWithNonStaticMethodException}).</li>
     * <li>{@code method} must be invokable on {@code instance} (signals a 
     * {@link MethodInstanceIncompatibilityException}).</li>
     * </ul>
     * 
     * @param instance an object.
     * @param method a method object. 
     * @throws BindingInstantiationException if the given instance and/or method do not constitute a valid binding.
     */
    @Override
    public void validate(Object instance, Method method) throws BindingInstantiationException {
      try {
        nonNull(method, "method");
        
        List<Exception> exceptions = new ArrayList<Exception>();
        if(! Modifier.isPublic(method.getModifiers()))
          exceptions.add(new InaccessibleMethodException(method));
        
        if(instance == null && ! Modifier.isStatic(method.getModifiers()))
          exceptions.add(new NullInstanceWithNonStaticMethodException(method));
        
        if(instance != null && ! method.getDeclaringClass().isAssignableFrom(instance.getClass()))
          exceptions.add(new MethodInstanceIncompatibilityException(instance, method));
        
        if(exceptions.size() > 0)
          throw new BindingInstantiationException(exceptions);
      } catch (BindingInstantiationException e) {
        throw e;
      } catch (Exception e) {
        throw new BindingInstantiationException(e);
      }
    }
  }
  
  protected static final Validator DEFAULT_VALIDATOR = new DefaultValidator();
  
  private Object instance;
  private Method method;
  
  /**
   * Constructs a new {@code Binding} object which holds a static method, using the default validator.
   * 
   * @param method a static method object.
   * @throws BindingInstantiationException if the given method is not deemed valid by the default validator.
   */
  public Binding(Method method) throws BindingInstantiationException {
    this(null, method, DEFAULT_VALIDATOR);
  }

  /**
   * Constructs a new {@code Binding} object, using the default validator.
   * 
   * @param instance an object.
   * @param method a method object.
   * @throws BindingInstantiationException if the given instance, method, or their combination is not deemed valid 
   * by the default validator.
   */
  public Binding(Object instance, Method method) throws BindingInstantiationException {
    this(instance, method, DEFAULT_VALIDATOR);
  }
  
  /**
   * Constructs a new {@code Binding} object, using the given validator. If no validator is given, no validation will 
   * be made.
   * 
   * @param instance an object.
   * @param method a method object.
   * @param validator the validator to be used on the given arguments. If {@code null}, no validation will be made.
   * @throws BindingInstantiationException if the given instance, method, or their combination is not deemed valid 
   * by the given validator.
   */
  public Binding(Object instance, Method method, Validator validator) throws BindingInstantiationException {
    if(validator != null)
      validator.validate(instance, method);
    
    // XXX workaround necessary due to bug 4819108 in the JVM
    // XXX but if one gets method from getDeclaredMethod it seems to work...
    if(Modifier.isPublic(method.getModifiers()))
      method.setAccessible(true);
    
    this.instance = instance;
    this.method = method;
  }

  // operations
  /**
   * Invokes this binding's method object on this binding's instance with the given arguments.
   * 
   * @param args the arguments for the method call.
   * @return the result of this binding's method invoked on this binding's instance with the given arguments.
   * @throws BindingInvocationException if an exception is thrown during the invocation.
   */
  public Object invoke(Object... args) throws BindingInvocationException {
    try {
      return getMethod().invoke(getInstance(), args);
    } catch(IllegalArgumentException e) {
      throw new BindingInvocationException(this, e);
    } catch(IllegalAccessException e) { 
      // should never happen if the validator does its job properly, 
      // but we all know how that goes... 
      throw new BindingInvocationException(this, e);
    } catch(InvocationTargetException e) {
      throw new BindingInvocationException(this, e);
    }
  }
  
  // utility methods
  /**
   * Returns a string representation of this object.
   * 
   * @return a string representation of this object.
   */
  @Override
  public String toString() {
    if(instance == null) // static method
      return "static " + getInstanceClass().getName() + "." + methodToString();
    
    return getInstance() + "." + methodToString();
  }
  
  /**
   * If two bindings hold the same instance and the same method, they will have the same hash code.
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    return prime * (prime + hashCodeOf(getInstance())) + hashCodeOf(getMethod());
  }

  /**
   * Two bindings are equal if their instances and their methods are equal.
   */
  @Override
  public boolean equals(Object obj) {
    if(this == obj)
      return true;
    
    if(obj == null || getClass() != obj.getClass())
      return false;
    
    Binding other = (Binding) obj;
    return areEqual(getInstance(), other.getInstance())
        && areEqual(getMethod(), other.getMethod());
  }
  
  // helper methods
  /* (non-Javadoc)
   * Returns a simplified string rendition of this object's method.
   */
  private String methodToString() {
    Class<?> instanceClass = getInstanceClass();
    String params = StringUtils.concatenate(", ", 
        getTypeNames(getExactParameterTypes(getMethod(), instanceClass)));
    
    return getMethod().getName() + "(" + params + "): " 
         + getTypeName(getExactReturnType(getMethod(), instanceClass));
  }
  
  // properties
  /**
   * Returns the most specific instance class compatible with this binding's instance and method object. That would be 
   * the instance's class, if the instance is not null, or the method's declaring class otherwise.
   * 
   * @return the most specific instance class compatible with this binding's instance and method object.
   */
  public Class<?> getInstanceClass() {
    return getInstance() != null 
         ? getInstance().getClass() 
         : getMethod().getDeclaringClass();
  }
  
  /**
   * Returns this binding's underlying instance. It may be {@code null} if this binding represents a static method.
   * 
   * @return this binding's underlying instance. 
   */
  public Object getInstance() {
    return instance;
  }
  
  /**
   * Returns this binding's underlying method object.
   * 
   * @return this binding's underlying method object. 
   */
  public Method getMethod() {
    return method;
  }
}
