package com.googlecode.transmuter.converter;

import static com.googlecode.gentyref.GenericTypeReflector.getExactParameterTypes;
import static com.googlecode.gentyref.GenericTypeReflector.getExactReturnType;
import static com.googlecode.transmuter.util.ObjectUtils.areEqual;
import static com.googlecode.transmuter.util.ObjectUtils.hashCodeOf;
import static com.googlecode.transmuter.util.ReflectionUtils.getTypeName;
import static com.googlecode.transmuter.util.ReflectionUtils.getTypeNames;
import static com.googlecode.transmuter.util.ReflectionUtils.isCompatible;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.googlecode.transmuter.converter.exception.InaccessibleMethodException;
import com.googlecode.transmuter.converter.exception.InvocationException;
import com.googlecode.transmuter.converter.exception.MethodInstanceIncompatibilityException;
import com.googlecode.transmuter.converter.exception.NullInstanceWithNonStaticMethodException;
import com.googlecode.transmuter.util.Notification;
import com.googlecode.transmuter.util.StringUtils;
import com.googlecode.transmuter.util.exception.MultipleCausesException;
import com.googlecode.transmuter.util.exception.NotificationNotFoundException;
import com.googlecode.transmuter.util.exception.ObjectInstantiationException;

/**
 * Represents an immutable invokable object, made binding a method to an object (which may be {@code null} if the 
 * method is static).
 * <p> 
 * Not all objects, methods or combinations thereof may be bound. The constructor will try to make sure that the 
 * given arguments are mutually compatible, throwing an exception if they are deemed otherwise. 
 * The default validation attempts to ensure that {@link #invoke(Object...) invoke} calls will not fail due to a 
 * method or object invalidity/incompatibility, i.e. that one cannot build an inherently uninvokable Binding.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class Binding {
  private Object instance;
  private Method method;
  
  /**
   * Makes a new {@code Binding} object which holds a static method.
   * 
   * @param method a static method object.
   * @throws ObjectInstantiationException if the given method is not deemed valid.
   * @see #initialize(Object, Method)
   */
  public Binding(Method method) throws ObjectInstantiationException {
    this(null, method);
  }

  /**
   * Constructs a new {@code Binding} object.
   * 
   * @param instance an object.
   * @param method a method object.
   * @throws ObjectInstantiationException if the given instance, method, or their combination is not deemed valid.
   * @see #initialize(Object, Method)
   */
  public Binding(Object instance, Method method) throws ObjectInstantiationException {
    initialize(instance, method);
  }

  /**
   * Called only in the constructors, this method attempts to validate the given arguments and populate this 
   * instance's fields, throwing an exception if the arguments are not deemed valid.
   * <p>
   * Delegates to {@link #tryInitialize(Object, Method) tryInitialize} for the actual legwork, so subclasses which 
   * wish to alter the initialization sequence should override it instead of this method.
   * <p> 
   * This method simply checks {@code tryInitialize}'s final report to determine if an exception must be thrown. 
   * The exception will hold all the problems found with the given arguments.
   * 
   * @param instance an object.
   * @param method a method object.
   * @throws ObjectInstantiationException if any problems were found during initialization.
   * @see #tryInitialize(Object, Method)
   */
  protected void initialize(Object instance, Method method) throws ObjectInstantiationException {
    try {
      Notification notification = tryInitialize(instance, method);
      
      if(notification == null)
        throw new ObjectInstantiationException(getClass(), new NotificationNotFoundException());
      
      if(notification.hasErrors())
        throw new ObjectInstantiationException(getClass(), notification.getErrors());
      
    } catch(ObjectInstantiationException e) {
      throw e;
    } catch(MultipleCausesException e) {
      // shouldn't happen (yeah, right :P), but in case it does...
      throw new ObjectInstantiationException(getClass(), e.getCauses());
    } catch(Exception e) {
      // dunno how we got here...
      throw new ObjectInstantiationException(getClass(), e);
    }
  }

  /**
   * Validates the given arguments, and populates this binding's fields accordingly. This method is protected so 
   * that subclasses can override it with their own initialization sequence.
   * <p>
   * This method returns a {@link Notification} object, which accumulates any problems verified here and reports the 
   * final status of the initialization. The following conditions are checked here:
   * 
   * <ul>
   * <li>{@code method} may not be {@code null}. This violation signals a single {@link IllegalArgumentException}.</li>
   * <li>{@code method} must have public visibility (signals an {@link InaccessibleMethodException}).</li>
   * <li>{@code method} must be static if {@code instance} is {@code null} (signals a 
   * {@link NullInstanceWithNonStaticMethodException}).</li>
   * <li>{@code method} must be invokable on {@code instance} (signals a 
   * {@link MethodInstanceIncompatibilityException}).</li>
   * </ul>
   * 
   * @param instance an object.
   * @param method a method object. 
   * @return a {@link Notification} with all errors found during validation e initialization. Cannot be null.
   */
  protected Notification tryInitialize(Object instance, Method method) {
    Notification notification = new Notification();
    
    if(method == null) // no point or way to check any further
      return notification.add(new IllegalArgumentException("method cannot be null!"));
    
    if(! Modifier.isPublic(method.getModifiers())) // why so antisocial?
      notification.add(new InaccessibleMethodException(method));
    
    if(instance == null && ! Modifier.isStatic(method.getModifiers())) // NullPointerException waiting to happen... 
      notification.add(new NullInstanceWithNonStaticMethodException(method));
    
    if(instance != null && ! isCompatible(method, instance.getClass()))
      notification.add(new MethodInstanceIncompatibilityException(instance, method));
    
    if(notification.hasErrors()) // errors were found, nothing more to do here
      return notification;
    
    // no errors found, time to fill the fields
    
    // XXX workaround necessary due to bug 4819108 in the JVM
    // XXX but if one gets method from getDeclaredMethod it seems to work...
    if(Modifier.isPublic(method.getModifiers()))
      method.setAccessible(true);
    
    this.instance = instance;
    this.method = method;
    
    return notification;
  }

  // operations
  /**
   * Invokes this binding's method object on this binding's instance with the given arguments.
   * 
   * @param args the arguments for the method call.
   * @return the result of this binding's method invoked on this binding's instance with the given arguments.
   * @throws InvocationException if an exception is thrown during the invocation.
   */
  public Object invoke(Object... args) throws InvocationException {
    try {
      return getMethod().invoke(getInstance(), args);
    } catch(IllegalArgumentException e) {
      throw new InvocationException(this, e);
    } catch(IllegalAccessException e) { 
      // should never happen if the validator does its job properly, 
      // but we all know how that goes... 
      throw new InvocationException(this, e);
    } catch(InvocationTargetException e) {
      throw new InvocationException(this, e);
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
  // ?? use ReflectionUtils.getOwnerType?
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
