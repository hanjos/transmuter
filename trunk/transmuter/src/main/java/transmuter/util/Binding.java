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

public class Binding {
  private final Object instance;
  private final Method method;
  
  public Binding(Method method) {
    this(null, method);
  }

  public Binding(Object instance, Method method) {
    validate(instance, method);
    
    this.instance = instance;
    this.method = method;
  }
  
  protected void validate(Object instance, Method method) throws BindingInstantiationException {
    if(method == null)
      throw new BindingInstantiationException(new IllegalArgumentException("method cannot be null!"));
    
    List<Exception> exceptions = new ArrayList<Exception>();
    if(! Modifier.isPublic(method.getModifiers()))
      exceptions.add(new InaccessibleMethodException(method));
    
    // workaround necessary due to bug 4819108 in the JVM
    // XXX but if method is from getDeclaredMethod everything seems to work...
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
  public Object invoke(Object... args) {
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
    Class<?> instanceType = getDeclaringType();
    String params = StringUtils.concatenate(", ", 
        getTypeNames(getExactParameterTypes(getMethod(), instanceType)));
    
    return getMethod().getName() + "(" + params + "): " 
         + getTypeName(getExactReturnType(getMethod(), instanceType));
  }
  
  // properties
  public Pair getPair() {
    return Pair.fromMethod(getMethod(), getDeclaringType());
  }

  public Class<?> getDeclaringType() {
    return getInstance() != null ? getInstance().getClass() : getMethod().getDeclaringClass();
  }
  
  public Object getInstance() {
    return instance;
  }
  
  public Method getMethod() {
    return method;
  }
}
