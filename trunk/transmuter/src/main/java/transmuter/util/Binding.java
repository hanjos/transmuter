package transmuter.util;

import static transmuter.util.ObjectUtils.areEqual;
import static transmuter.util.ObjectUtils.hashCodeOf;

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
  
  public Binding(Object instance, Method method) {
    validate(instance, method);
    
    this.instance = instance;
    this.method = method;
  }

  private void validate(Object instance, Method method) throws BindingInstantiationException {
    if(method == null)
      throw new BindingInstantiationException(new IllegalArgumentException("method cannot be null!"));
    
    List<RuntimeException> exceptions = new ArrayList<RuntimeException>();
    if(! Modifier.isPublic(method.getModifiers()))
      exceptions.add(new InaccessibleMethodException(method));
      
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
      return method.invoke(instance, args);
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
  public int hashCode() {
    final int prime = 31;
    return prime * (prime + hashCodeOf(instance)) + hashCodeOf(method);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj)
      return true;
    
    if(obj == null || getClass() != obj.getClass())
      return false;
    
    Binding other = (Binding) obj;
    return areEqual(instance, other.instance)
        && areEqual(method, other.method);
  }

  // properties
  public Object getInstance() {
    return instance;
  }
  
  public Method getMethod() {
    return method;
  }
}
