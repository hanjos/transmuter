package transmuter;

import static transmuter.util.ObjectUtils.nonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import transmuter.exception.ConverterCollisionException;
import transmuter.exception.ConverterRegistrationException;
import transmuter.exception.MultipleCausesException;
import transmuter.exception.NoCompatibleConvertersFoundException;
import transmuter.exception.PairIncompatibleWithBindingException;
import transmuter.exception.TooManyConvertersFoundException;
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
      
      // check if the binding matches the pair
      if(! pair.isAssignableFrom(binding.getPair()))
        throw new PairIncompatibleWithBindingException(pair, binding);
      
      // check for collisions here
      final Map<Pair, Binding> converterMap = Transmuter.this.getConverterMap();
      if(converterMap != this) {
        if(containsKey(pair)) {
          if(binding.equals(get(pair))) // redundant call, ignore it
            return null;
  
          // converter collision, throw up
          throw new ConverterCollisionException(pair, binding, get(pair));
        }
      }
      
      // check for collisions in the transmuter map (this != trans)
      if(converterMap.containsKey(pair))  {
        if(binding.equals(converterMap.get(pair))) // redundant call, ignore it
          return null;

        // converter collision, throw up
        throw new ConverterCollisionException(pair, binding, converterMap.get(pair));
      }
      
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
  public <From, To, SubFrom extends From> To convert(SubFrom from, Class<From> fromType, Class<To> toType) {
    return convert(from, TypeToken.get(fromType), TypeToken.get(toType));
  }
  
  // TODO type compatibility
  @SuppressWarnings("unchecked")
  public <From, To, SubFrom extends From> To convert(SubFrom from, TypeToken<From> fromType, TypeToken<To> toType) {
    nonNull(fromType); nonNull(toType);
    
    return (To) getConverterFor(new Pair(fromType, toType)).invoke(from);
  }

  public void register(Object object) {
    if(object == null)
      return;
    
    Map<Pair, Binding> temp = new PairBindingMap();
    List<Exception> exceptions = new ArrayList<Exception>();
    
    for(Method method : object.getClass().getMethods()) {
      if(! method.isAnnotationPresent(Converts.class))
        continue;
      
      try {
        temp.put(Pair.fromMethod(method, object.getClass()), new Binding(object, method));
      } catch(Exception e) {
        if(! (e instanceof MultipleCausesException))
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
    unregister(new Pair(fromType, toType));
  }
  
  public void unregister(TypeToken<?> fromType, TypeToken<?> toType) {
    unregister(new Pair(fromType, toType));
  }
  
  public Binding unregister(Pair pair) {
    return getConverterMap().remove(pair);
  }
  
  // helper methods
  protected Binding getConverterFor(Pair pair) {
    if(pair == null)
      throw new NoCompatibleConvertersFoundException(pair);
    
    Binding converter = getMostCompatibleConverterFor(pair);
    if(converter != null)
      return converter;
    
    List<Binding> compatibleBindings = getCompatibleConvertersFor(pair);
    
    if(compatibleBindings.isEmpty())
      throw new NoCompatibleConvertersFoundException(pair);
    
    if(compatibleBindings.size() > 1)
      throw new TooManyConvertersFoundException(pair, compatibleBindings);
    
    return compatibleBindings.get(0);
  }

  protected Binding getMostCompatibleConverterFor(Pair pair) {
    if(pair == null)
      return null;
    
    Binding binding = getConverterMap().get(pair);
    if(binding != null)
      return binding;
    
    // TODO determine a search algorithm for a "most compatible" pair
    // TODO parameterize it?
    
    return null;
  }
  
  protected List<Binding> getCompatibleConvertersFor(Pair pair) {
    List<Binding> compatibleBindings = new ArrayList<Binding>();
    if(pair == null)
      return compatibleBindings;
    
    for(Entry<Pair, Binding> entry : getConverterMap().entrySet()) {
      if(entry.getKey().isAssignableFrom(pair))
        compatibleBindings.add(entry.getValue());
    }
    
    return compatibleBindings;
  } 
  
  // properties
  protected Map<Pair, Binding> getConverterMap() {
    return converterMap;
  }
}