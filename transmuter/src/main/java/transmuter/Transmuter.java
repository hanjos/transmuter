package transmuter;

import static transmuter.util.ObjectUtils.nonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import transmuter.exception.ConverterCollisionException;
import transmuter.exception.ConverterRegistrationException;
import transmuter.exception.InvalidReturnTypeException;
import transmuter.exception.MultipleCausesException;
import transmuter.exception.PairIncompatibleWithBindingException;
import transmuter.exception.SameClassConverterCollisionException;
import transmuter.exception.WrongParameterCountException;
import transmuter.type.TypeToken;
import transmuter.util.Binding;
import transmuter.util.Pair;

public class Transmuter {
  private class PairBindingMap extends HashMap<Pair, Binding> {
    private static final long serialVersionUID = 1L;

    public PairBindingMap() { /* empty block */ }

    // TODO pile up the exceptions like in register?
    @Override
    public Binding put(Pair pair, Binding binding) {
      nonNull(pair, "pair"); nonNull(binding, "binding");
      
      // check for collisions here
      final Method bindingMethod = binding.getMethod();
      if(containsKey(pair)) {
        if(binding.equals(get(pair))) // redundant call, ignore it
          return null;

        // converter collision, throw up
        throw new SameClassConverterCollisionException(
            Arrays.asList(bindingMethod, get(pair).getMethod()), 
            TypeToken.get(bindingMethod.getDeclaringClass()), 
            pair);
      }
      
      // check for collisions in the transmuter
      final Map<Pair, Binding> converterMap = Transmuter.this.getConverterMap();
      if(converterMap.containsKey(pair))  {
        if(binding.equals(converterMap.get(pair))) // redundant call, ignore it
          return null;

        // converter collision, throw up
        throw new ConverterCollisionException(
            Arrays.asList(bindingMethod, converterMap.get(pair).getMethod()), 
            pair);
      }
      
      // check if the binding matches the pair
      if(! pair.isAssignableFrom(Transmuter.this.extractTypes(bindingMethod)))
        throw new PairIncompatibleWithBindingException(pair, binding);
      
      return super.put(pair, binding);
    }
    
    @Override
    public void putAll(Map<? extends Pair, ? extends Binding> map) {
      if(map == null || map.isEmpty())
        return;
      
      super.putAll(map);
    }
    
    @Override
    public boolean containsKey(Object key) {
      if(key == null)
        return false;
      
      return super.containsKey(key);
    }
    
    @Override
    public Binding remove(Object key) {
      if(key == null)
        return null;
      
      return super.remove(key);
    }
  }
  
  private Map<Pair, Binding> converterMap;
  
  public Transmuter() {
    converterMap = new PairBindingMap();
  }
  
  // operations
  public void register(Object object) {
    if(object == null)
      return;
    
    Map<Pair, Binding> temp = new PairBindingMap();
    List<Exception> exceptions = new ArrayList<Exception>();
    
    for(Method method : object.getClass().getMethods()) {
      if(! method.isAnnotationPresent(Converter.class))
        continue;
      
      try {
        temp.put(extractTypes(method), new Binding(object, method));
      } catch(Exception e) {
        if(e.getClass() != MultipleCausesException.class)
          exceptions.add(e);
        else
          exceptions.addAll(((MultipleCausesException) e).getCauses());
      }
    }
    
    if(exceptions.size() > 0) // errors during registration
      throw new ConverterRegistrationException(exceptions);
    
    getConverterMap().putAll(temp);
  }
  
  public boolean isRegistered(Type fromType, Type toType) {
    return isRegistered(new Pair(fromType, toType));
  }
  
  public boolean isRegistered(TypeToken<?> fromType, TypeToken<?> toType) {
    return isRegistered(new Pair(fromType, toType));
  }
  
  public boolean isRegistered(Pair pair) {
    return getConverterMap().containsKey(pair);
  }
  
  public void unregister(Type fromType, Type toType) {
    unregister(TypeToken.get(fromType), TypeToken.get(toType));
  }
  
  public void unregister(TypeToken<?> fromType, TypeToken<?> toType) {
    unregister(new Pair(fromType, toType));
  }
  
  // helper operations
  protected Pair extractTypes(Method method) {
    List<Exception> exceptions = new ArrayList<Exception>();
    
    if(method == null)
      exceptions.add(new IllegalArgumentException("method"));
    
    final Type[] parameterTypes = method.getGenericParameterTypes();
    final int parameterCount = parameterTypes.length;
    if(parameterCount == 0 || parameterCount > 1)
      exceptions.add(new WrongParameterCountException(method, 1));
    
    final Type returnType = method.getGenericReturnType();
    if(TypeToken.ValueType.VOID.matches(returnType))
      exceptions.add(new InvalidReturnTypeException(method));
    
    TypeToken<?> parameterToken = null;
    TypeToken<?> returnToken = null;
    
    if(parameterCount == 1) {
      try {
        parameterToken = TypeToken.get(parameterTypes[0]);
      } catch(Exception e) { // type token building may throw exceptions
        exceptions.add(e);
      }
    }
    
    try {
      returnToken = TypeToken.get(returnType);
    } catch(Exception e) { // type token building may throw exceptions
      exceptions.add(e);
    }
    
    if(exceptions.size() > 0)
      throw new MultipleCausesException(exceptions);
    
    return new Pair(parameterToken, returnToken);
  }
  
  protected Binding unregister(Pair pair) {
    return getConverterMap().remove(pair);
  }
  
  protected Map<Pair, Binding> getConverterMap() {
    return converterMap;
  }
}
