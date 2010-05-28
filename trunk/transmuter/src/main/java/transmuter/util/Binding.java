package transmuter.util;

import static transmuter.util.ObjectUtils.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import transmuter.util.exception.BindingInvocationException;

public class Binding {
  private final Object instance;
  private final Method method;
  
  // TODO ensure method with compatible with object
  public Binding(Object instance, Method method) {
    this.instance = instance;
    this.method = nonNull(method);
  }

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
