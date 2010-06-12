package transmuter.util;

import static com.googlecode.gentyref.GenericTypeReflector.getExactParameterTypes;
import static com.googlecode.gentyref.GenericTypeReflector.getExactReturnType;
import static transmuter.util.ObjectUtils.areEqual;
import static transmuter.util.ObjectUtils.hashCodeOf;
import static transmuter.util.ReflectionUtils.getTypeName;
import static transmuter.util.ReflectionUtils.getTypeNames;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import transmuter.util.exception.BindingInstantiationException;
import transmuter.util.exception.BindingInvocationException;
import transmuter.util.exception.InaccessibleMethodException;
import transmuter.util.exception.MethodInstanceIncompatibilityException;
import transmuter.util.exception.NullInstanceWithNonStaticMethodException;

/**
 * Represents an immutable invokable object, made binding a method 
 * to an instance (which may be {@code null} if the method is static).
 * <p> 
 * Not all objects, methods or combinations thereof may be bound. 
 * Validation is performed in the constructor 
 * (using {@link #validate(Object, Method) validate}), which will throw an 
 * exception if the object and the method are deemed invalid or incompatible. 
 * The standard validation attempts to ensure that 
 * {@link #invoke(Object...) invoke} calls will not fail due to a method or
 * object invalidity/incompatibility.
 * 
 * @author Humberto S. N. dos Anjos
 */
public class Binding {
  private Object instance;
  private Method method;
  
  /**
   * Constructs a new {@code Binding} object which represents a static method
   * bound to {@code null}.
   * 
   * @param method a static method object.
   * @throws BindingInstantiationException if {@code method} is not deemed 
   * valid by {@link #validate(Object, Method)}.
   */
  public Binding(Method method) throws BindingInstantiationException {
    this(null, method);
  }

  /**
   * Constructs a new {@code Binding} object.
   * 
   * @param instance an object.
   * @param method a method object.
   * @throws BindingInstantiationException if {@code instance}, {@code method}, or
   * their combination is not deemed valid by {@link #validate(Object, Method)}.
   */
  public Binding(Object instance, Method method) throws BindingInstantiationException {
    validate(instance, method);
    
    this.instance = instance;
    this.method = method;
  }
  
  /**
   * Checks if {@code instance} and {@code method} can be bound.
   * <p>
   * Several checks are made, with each one of them having a corresponding 
   * exception, which is collected and bundled into a 
   * {@link BindingInstantiationException}. The restrictions implemented 
   * here are:
   * 
   * <ul>
   * <li>{@code method} may not be {@code null}. This violation throws a 
   * BindingInstantiationException with a single {@link IllegalArgumentException}.</li>
   * <li>{@code method} must have public visibility (signals an {@link InaccessibleMethodException}).</li>
   * <li>{@code method} must be static if {@code instance} is {@code null} (signals a 
   * {@link NullInstanceWithNonStaticMethodException}).</li>
   * <li>{@code method} must be callable from {@code instance} (signals a
   * {@link MethodInstanceIncompatibilityException}).</li>
   * </ul>
   * 
   * @param instance an object
   * @param method a method object. 
   * @throws BindingInstantiationException if {@code instance} and/or {@code method} do not
   * constitute a valid binding.
   */
  protected void validate(Object instance, Method method) throws BindingInstantiationException {
    if(method == null)
      throw new BindingInstantiationException(new IllegalArgumentException("method cannot be null!"));
    
    List<Exception> exceptions = new ArrayList<Exception>();
    if(! Modifier.isPublic(method.getModifiers()))
      exceptions.add(new InaccessibleMethodException(method));
    
    // workaround necessary due to bug 4819108 in the JVM
    // XXX but if one gets method from getDeclaredMethod it seems to work...
    if(Modifier.isPublic(method.getModifiers()))
      method.setAccessible(true);
    
    if(instance == null && ! Modifier.isStatic(method.getModifiers()))
      exceptions.add(new NullInstanceWithNonStaticMethodException(method));
    
    if(instance != null && ! method.getDeclaringClass().isAssignableFrom(instance.getClass()))
      exceptions.add(new MethodInstanceIncompatibilityException(instance, method));
    
    if(exceptions.size() > 0)
      throw new BindingInstantiationException(exceptions);
  }

  // operations
  /**
   * Invokes this binding's method object on this binding's instance with the 
   * given arguments.
   * 
   * @param args the arguments in the method call.
   * @return the result of this binding's method invoked on
   * this binding's instance with the arguments in {@code args}.
   * @throws BindingInvocationException if an exception is thrown during the
   * invocation.
   */
  public Object invoke(Object... args) throws BindingInvocationException {
    try {
      return getMethod().invoke(getInstance(), args);
    } catch(IllegalArgumentException e) {
      throw new BindingInvocationException(this, e);
    } catch(IllegalAccessException e) {
      throw new BindingInvocationException(this, e);
    } catch(InvocationTargetException e) {
      throw new BindingInvocationException(this, e);
    }
  }
  
  // utility methods
  @Override
  public String toString() {
    if(instance == null) // static method
      return "Binding[static " + methodToString() + "]";
    
    return "Binding[" + getInstance() + "." + methodToString() + "]";
  }
  
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
  private String methodToString() {
    Class<?> instanceClass = getInstanceClass();
    String params = StringUtils.concatenate(", ", 
        getTypeNames(getExactParameterTypes(getMethod(), instanceClass)));
    
    return getMethod().getName() + "(" + params + "): " 
         + getTypeName(getExactReturnType(getMethod(), instanceClass));
  }
  
  // properties
  /**
   * Returns the most specific instance class compatible with this binding's 
   * instance and method object. That would be the instance's class, if the 
   * instance is not null, or the method's declaring class otherwise.
   * 
   * @return the most specific instance class compatible with 
   * this binding's instance and method object.
   */
  public Class<?> getInstanceClass() {
    return getInstance() != null 
         ? getInstance().getClass() 
         : getMethod().getDeclaringClass();
  }
  
  /**
   * @return this binding's underlying instance. 
   */
  public Object getInstance() {
    return instance;
  }
  
  /**
   * @return this binding's underlying method object. 
   */
  public Method getMethod() {
    return method;
  }
}
