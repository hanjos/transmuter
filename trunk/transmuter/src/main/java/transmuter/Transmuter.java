package transmuter;

import static transmuter.util.ObjectUtils.areEqual;
import static transmuter.util.ObjectUtils.classOf;
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

public class Transmuter {
  protected static class PairBindingMap extends HashMap<Pair, Binding> {
    private static final long serialVersionUID = 1L;

    public PairBindingMap() { /* empty block */ }

    @Override
    public Binding put(Pair pair, Binding binding) {
      return validatePut(pair, binding) 
           ? null
           : super.put(pair, binding);
    }

    protected boolean validatePut(Pair pair, Binding binding) {
      // check if the binding matches the pair
      checkForCompatibility(pair, binding);
      
      // check for collisions here
      return checkForCollision(pair, binding);
    }

    protected void checkForCompatibility(Pair pair, Binding binding) 
    throws PairIncompatibleWithBindingException {
      nonNull(pair, "pair"); 
      nonNull(binding, "binding");
      
      if(! pair.isAssignableFrom(Pair.fromBinding(binding)))
        throw new PairIncompatibleWithBindingException(pair, binding);
    }

    protected boolean checkForCollision(Pair pair, Binding binding) 
    throws ConverterCollisionException {
      return checkMapForCollision(pair, binding, this);
    }
    
    protected static boolean checkMapForCollision(Pair pair, Binding binding, Map<? extends Pair, ? extends Binding> map) 
    throws ConverterCollisionException {
      nonNull(pair, "pair"); 
      nonNull(binding, "binding");
      nonNull(map, "map");
      
      if(! map.containsKey(pair))
        return false;
      
      if(areEqual(binding, map.get(pair)))
        return true;
      
      // converter collision, throw up
      throw new ConverterCollisionException(pair, binding, map.get(pair));
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
    
  protected static class TempPairBindingMap extends PairBindingMap {
    private static final long serialVersionUID = 1L;

    private Map<Pair, Binding> masterMap;

    public TempPairBindingMap(Map<Pair, Binding> masterMap) {
      this.masterMap = nonNull(masterMap);
    }
    
    @Override
    protected boolean checkForCollision(Pair pair, Binding binding) 
    throws ConverterCollisionException {
      return super.checkForCollision(pair, binding)
          || super.checkMapForCollision(pair, binding, getMasterMap());
    }
    
    public Map<Pair, Binding> getMasterMap() {
      return masterMap;
    }
  }
  
  private Map<Pair, Binding> converterMap;
  
  public Transmuter() {
    converterMap = new PairBindingMap();
  }
  
  // operations
  // XXX what about erasure types?
  public <From, To> To convert(From from, Class<To> toType) {
    return convert(from, TypeToken.get(toType));
  }
  
  @SuppressWarnings("unchecked")
  public <From, To> To convert(From from, TypeToken<To> toType) {
    return convert(from, TypeToken.get((Class<From>) classOf(from)), toType);
  }

  public <From, To, SubFrom extends From> To convert(SubFrom from, Class<From> fromType, Class<To> toType) {
    return convert(from, TypeToken.get(fromType), TypeToken.get(toType));
  }
  
  @SuppressWarnings("unchecked")
  public <From, To, SubFrom extends From> To convert(SubFrom from, TypeToken<From> fromType, TypeToken<To> toType) {
    return (To) convertRaw(from, fromType, toType);
  }

  protected Object convertRaw(Object from, TypeToken<?> toType) {
    return convertRaw(from, TypeToken.get(classOf(from)), toType);
  }
  
  protected Object convertRaw(Object from, TypeToken<?> fromType, TypeToken<?> toType) {
    nonNull(fromType, "fromType"); nonNull(toType, "toType");
    
    return getConverterFor(new Pair(fromType, toType)).invoke(from);
  }
  
  public void register(Object object) {
    if(object == null)
      return;
    
    Map<Pair, Binding> temp = new TempPairBindingMap(getConverterMap());
    List<Exception> exceptions = new ArrayList<Exception>();
    
    for(Method method : object.getClass().getMethods()) {
      if(! method.isAnnotationPresent(Converts.class))
        continue;
      
      try {
        temp.put(Pair.fromMethod(method, object.getClass()), new Binding(object, method));
      } catch (MultipleCausesException e) {
        exceptions.addAll(e.getCauses());
      } catch(Exception e) {
        exceptions.add(e);
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
    
    // TODO determine a search algorithm for a "most compatible" pair
    // XXX parameterize it?
    
    return getConverterMap().get(pair);
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
