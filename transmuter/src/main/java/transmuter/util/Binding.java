package transmuter.util;

import static transmuter.util.ObjectUtils.areEqual;
import static transmuter.util.ObjectUtils.hashCodeOf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import transmuter.util.exception.BindingInstantiationException;
import transmuter.util.exception.BindingInvocationException;
import transmuter.util.exception.InaccessibleMethodException;
import transmuter.util.exception.InaccessibleObjectTypeException;
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
    
    List<Exception> exceptions = new ArrayList<Exception>();
    if(! Modifier.isPublic(method.getModifiers()))
      exceptions.add(new InaccessibleMethodException(method));
    
    if(instance == null && ! Modifier.isStatic(method.getModifiers()))
      exceptions.add(new NullInstanceWithNonStaticMethodException(method));
    
    if(instance != null && ! Modifier.isPublic(instance.getClass().getModifiers()))
      exceptions.add(new InaccessibleObjectTypeException(instance));
    
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
  public String toString() {
    if(instance == null) // static method
      return "Binding[static " + methodToString() + "]";
    
    return "Binding[" + instance + "." + methodToString() + "]";
  }
  
  private String methodToString() {
    String[] params = ReflectionUtils.getTypeNames(method.getGenericParameterTypes());
    String paramsAsString = Arrays.toString(params);
    if(paramsAsString.startsWith("[") && paramsAsString.endsWith("]"))
      paramsAsString = paramsAsString.substring(1, paramsAsString.length() - 1);
    
    String returnTypeAsString = ReflectionUtils.getTypeName(method.getGenericReturnType());
    
    return method.getName() + "(" + paramsAsString + "): " + returnTypeAsString;
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
