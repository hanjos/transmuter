package transmuter.util;

import static transmuter.util.ObjectUtils.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Binding {
  private final Object instance;
  private final Method method;
  
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
  
  // properties
  public Object getInstance() {
    return instance;
  }
  
  public Method getMethod() {
    return method;
  }
}
